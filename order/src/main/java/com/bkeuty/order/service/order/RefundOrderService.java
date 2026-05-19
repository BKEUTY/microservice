package com.bkeuty.order.service.order;

import com.bkeuty.order.dto.auth.TokenValidationResponseDto;
import com.bkeuty.order.dto.order.CreateRefundOrderRequestDto;
import com.bkeuty.order.dto.order.RefundOrderEventDto;
import com.bkeuty.order.dto.order.UserRefundOrderDto;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.entity.RefundOrder;
import com.bkeuty.order.enums.OrderStatus;
import com.bkeuty.order.repository.OrderItemRepository;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.RefundOrderRepository;
import com.bkeuty.order.service.S3Service;
import com.bkeuty.order.util.OrderAddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class RefundOrderService {

    private static final int REFUND_WINDOW_DAYS = 7;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefundOrderRepository refundOrderRepository;
    private final KafkaTemplate<String, Object> kafkaEventTemplate;
    private final S3Service s3Service;

    public RefundOrderService(OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              RefundOrderRepository refundOrderRepository,
                              KafkaTemplate<String, Object> kafkaEventTemplate,
                              S3Service s3Service) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.refundOrderRepository = refundOrderRepository;
        this.kafkaEventTemplate = kafkaEventTemplate;
        this.s3Service = s3Service;
    }

    /**
     * Creates a refund order for the given order items.
     *
     * <p>Validates that:
     * <ul>
     *   <li>The order exists and belongs to the requesting user.</li>
     *   <li>The order status is {@link OrderStatus#SUCCEEDED}.</li>
     *   <li>The delivery date is within the {@value #REFUND_WINDOW_DAYS}-day refund window.</li>
     * </ul>
     *
     * <p>Images are uploaded to S3 under
     * {@code refund-evident/{refundOrderId}/{uuid}_{filename}} after the RefundOrder
     * is persisted (so the DB-assigned ID is available for the S3 folder name).
     * After saving the refund, a Kafka event is published to {@code refund-order-topic}.
     *
     * @param userInfo validated token information of the requesting user
     * @param request  refund request body
     * @param images   optional evidence images (may be null or empty)
     * @return the persisted {@link RefundOrder} with S3 image URLs set
     */
    public RefundOrder createRefundOrder(TokenValidationResponseDto userInfo,
                                        CreateRefundOrderRequestDto request,
                                        List<MultipartFile> images) {

        // 1. Fetch and validate the order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUserId().equals(userInfo.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this order");
        }

        // 2. Check order status is SUCCEEDED
        if (order.getStatus() != OrderStatus.SUCCEEDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Refund is only allowed for orders that have been successfully delivered");
        }

        // 3. Check delivery within 2-day refund window
        LocalDateTime deliveryDateTime = order.getDeliveryDate(); // UTC+7 local time

        if (deliveryDateTime == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order does not have a recorded delivery date");
        }

// Define timezone explicitly
        ZoneId orderZone = ZoneId.of("Asia/Ho_Chi_Minh");

// Convert to ZonedDateTime
        ZonedDateTime deliveryZoned = deliveryDateTime.atZone(orderZone);

// Add refund window
        ZonedDateTime refundDeadline =
                deliveryZoned.plusDays(REFUND_WINDOW_DAYS);

// Compare with current time in same timezone
        if (ZonedDateTime.now(orderZone).isAfter(refundDeadline)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Refund window has expired. Refunds must be requested within "
                            + REFUND_WINDOW_DAYS + " days of delivery");
        }

        // 4. Validate and collect the requested order items
        if (request.getOrderItemId() == null || request.getOrderItemId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one order item must be selected for refund");
        }

        List<OrderItem> itemsToRefund = new ArrayList<>();
        BigDecimal refundTotal = BigDecimal.ZERO;

        for (Integer itemId : request.getOrderItemId()) {
            OrderItem item = orderItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Order item not found: " + itemId));

            if (item.getOrder() == null || !item.getOrder().getId().equals(request.getOrderId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order item " + itemId + " does not belong to order " + request.getOrderId());
            }

            if (item.getRefundOrder() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order item " + itemId + " has already been submitted for a refund");
            }

            refundTotal = refundTotal.add(
                    item.calculateUnitRefundAmount().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemsToRefund.add(item);
        }

        // 5. Build and persist the RefundOrder (without images yet – we need the DB id for the S3 folder)
        String fromAddressString = request.getFromAddress() != null
                ? OrderAddressUtils.addressDtoToAddress(request.getFromAddress())
                : null;

        RefundOrder refundOrder = RefundOrder.builder()
                .userId(userInfo.getUserId())
                .orderId(request.getOrderId())
                .total(refundTotal)
                .fromAddress(fromAddressString)
                .phoneNumber(request.getPhoneNumber())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        RefundOrder savedRefundOrder = refundOrderRepository.saveAndFlush(refundOrder);

        // 6. Upload images to S3 using the persisted refundOrderId as the sub-folder
        List<String> imageUrls = s3Service.uploadRefundEvidenceImages(savedRefundOrder.getId(), images);
        if (!imageUrls.isEmpty()) {
            savedRefundOrder.setEvidenceImageUrls(imageUrls);
            savedRefundOrder = refundOrderRepository.save(savedRefundOrder);
        }

        // 7. Link order items to the refund order
        for (OrderItem item : itemsToRefund) {
            item.setRefundOrder(savedRefundOrder);
            orderItemRepository.save(item);
        }

        // 8. Publish Kafka event
        List<Integer> orderItemIds = itemsToRefund.stream()
                .map(OrderItem::getId)
                .collect(Collectors.toList());

        RefundOrderEventDto event = RefundOrderEventDto.builder()
                .refundOrderId(savedRefundOrder.getId())
                .orderId(request.getOrderId())
                .userId(userInfo.getUserId())
                .total(refundTotal)
                .fromAddress(fromAddressString)
                .phoneNumber(request.getPhoneNumber())
                .orderItemIds(orderItemIds)
                .evidenceImageUrls(imageUrls)
                .note(request.getNote())
                .build();

//        kafkaEventTemplate.send("refund-order-topic", event);
        log.info("Published refund-order-topic event for refundOrderId={}", savedRefundOrder.getId());

        return savedRefundOrder;
    }

    public Page<UserRefundOrderDto> getUserRefundOrders(TokenValidationResponseDto userInfo, Pageable pageable) {
        Page<RefundOrder> refunds = refundOrderRepository.findByUserId(userInfo.getUserId(), pageable);
        return refunds.map(r -> UserRefundOrderDto.builder()
                .id(r.getId())
                .orderId(r.getOrderId())
                .total(r.getTotal())
                .fromAddress(r.getFromAddress())
                .phoneNumber(r.getPhoneNumber())
                .note(r.getNote())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .evidenceImageUrls(r.getEvidenceImageUrls())
                .items(r.getOrderItems() != null ? r.getOrderItems().stream()
                        .map(OrderItem::getProductVariantName)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build());
    }
}
