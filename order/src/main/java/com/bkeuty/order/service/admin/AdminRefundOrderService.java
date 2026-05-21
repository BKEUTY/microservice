package com.bkeuty.order.service.admin;

import com.bkeuty.order.dto.admin.AdminRefundOrderDto;
import com.bkeuty.order.dto.order.ProcessRefundEventDto;
import com.bkeuty.order.dto.shipping.*;
import com.bkeuty.order.entity.Order;
import com.bkeuty.order.entity.OrderItem;
import com.bkeuty.order.entity.RefundOrder;
import com.bkeuty.order.enums.RefundStatus;
import com.bkeuty.order.repository.OrderRepository;
import com.bkeuty.order.repository.RefundOrderRepository;
import com.bkeuty.order.util.OrderAddressUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AdminRefundOrderService {

    @Value("${ghn.return-address}")
    private String returnAddress;
    @Value("${ghn.return-phone}")
    private String returnPhone;
    @Value("${ghn.return-ward}")
    private String returnWard;
    @Value("${ghn.return-district}")
    private String returnDistrict;
    @Value("${ghn.return-province}")
    private String returnProvince;
    private final RefundOrderRepository refundOrderRepository;
    private final KafkaTemplate<String, Object> kafkaEventTemplate;
    private final KafkaTemplate<String, CreateRefundShippingMessage> kafkaCreateShippingOrderTemplate;
    private final OrderRepository orderRepository;

    public AdminRefundOrderService(RefundOrderRepository refundOrderRepository,
            KafkaTemplate<String, Object> kafkaEventTemplate,
            OrderRepository orderRepository, KafkaTemplate<String, CreateRefundShippingMessage> kafkaCreateShippingOrderTemplate) {
        this.refundOrderRepository = refundOrderRepository;
        this.kafkaEventTemplate = kafkaEventTemplate;
        this.orderRepository = orderRepository;
        this.kafkaCreateShippingOrderTemplate = kafkaCreateShippingOrderTemplate;
    }

    // -----------------------------------------------------------------------
    // List
    // -----------------------------------------------------------------------

    /**
     * Returns a paginated list of refund orders, optionally filtered by status.
     *
     * @param pageable pagination settings
     * @param status   optional {@link RefundStatus} name (case-insensitive)
     * @return page of {@link AdminRefundOrderDto}
     */
    public Page<AdminRefundOrderDto> getAllRefundOrders(Pageable pageable, String status) {
        Specification<RefundOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                String trimmed = status.trim().toUpperCase(Locale.ROOT);
                try {
                    predicates.add(cb.equal(root.get("status"), RefundStatus.valueOf(trimmed)));
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid refund status: " + trimmed
                                    + ". Allowed values: " + Arrays.toString(RefundStatus.values()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<RefundOrder> page = refundOrderRepository.findAll(spec, pageable);
        if (page.isEmpty())
            return Page.empty(pageable);

        List<AdminRefundOrderDto> dtos = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    /**
     * Returns a single refund order by ID.
     */
    public AdminRefundOrderDto getRefundOrderById(Integer refundOrderId) {
        RefundOrder refundOrder = findOrThrow(refundOrderId);
        return toDto(refundOrder);
    }

    // -----------------------------------------------------------------------
    // Approve
    // -----------------------------------------------------------------------

    /**
     * Approves a refund order that is currently in {@link RefundStatus#PENDING}
     * state.
     * Transitions status → {@link RefundStatus#APPROVED}.
     */
    public AdminRefundOrderDto approveRefundOrder(Integer refundOrderId) {
        RefundOrder refundOrder = findOrThrow(refundOrderId);

        if (refundOrder.getStatus() != RefundStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PENDING refund orders can be approved. Current status: " + refundOrder.getStatus());
        }
        AddressDto addressDto = OrderAddressUtils.toAddressDto(refundOrder.getFromAddress());
        refundOrder.setStatus(RefundStatus.APPROVED);
        RefundOrder saved = refundOrderRepository.save(refundOrder);
        CreateRefundShippingDto createRefundShippingDto = CreateRefundShippingDto.builder()
                                                                    .fromName(refundOrder.getPhoneNumber())
                                                                    .fromPhone(refundOrder.getPhoneNumber())
                                                                    .fromAddress(addressDto.getAddress())
                                                                    .fromWardName(addressDto.getWard().getWardName())
                                                                    .fromDistrictName(addressDto.getDistrict().getDistrictName())
                                                                    .fromProvinceName(addressDto.getProvince().getProvinceName())
                                                                    .toAddress(returnAddress)
                                                                    .toName(returnPhone)
                                                                    .toPhone(returnPhone)
                                                                    .toWardName(returnWard)
                                                                    .toDistrictName(returnDistrict)
                                                                    .toProvinceName(returnProvince)
                                                                    .items(refundOrder.getOrderItems().stream().map(this::toShippingItemDto).toList())
                                                                    .build();

        log.info("RefundOrder {} approved by admin", refundOrderId);
        System.out.println("Order Service admin click approve refund and send kafka");
        kafkaCreateShippingOrderTemplate.send("create-refund-shipping-topic", CreateRefundShippingMessage.builder().refundOrderId(refundOrderId).createRefundShippingDto(createRefundShippingDto).build());
        return toDto(saved);
    }
    private ShippingItemDto toShippingItemDto(OrderItem dto) {
        return ShippingItemDto.builder()
                .name(dto.getProductVariantName())
                .quantity(dto.getQuantity())
                .price(dto.getProductVariantPrice().intValue())
                .build();
    }
    /**
     * Rejects a refund order that is currently in {@link RefundStatus#PENDING}
     * state.
     * Transitions status → {@link RefundStatus#REJECTED}.
     */
    public AdminRefundOrderDto rejectRefundOrder(Integer refundOrderId) {
        RefundOrder refundOrder = findOrThrow(refundOrderId);

        if (refundOrder.getStatus() != RefundStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PENDING refund orders can be rejected. Current status: " + refundOrder.getStatus());
        }

        refundOrder.setStatus(RefundStatus.REJECTED);
        RefundOrder saved = refundOrderRepository.save(refundOrder);
        log.info("RefundOrder {} rejected by admin", refundOrderId);
        return toDto(saved);
    }

    // -----------------------------------------------------------------------
    // Complete (mark as ready for money refund)
    // -----------------------------------------------------------------------

    /**
     * Marks an {@link RefundStatus#APPROVED} refund order as
     * {@link RefundStatus#DELIVERED},
     * signalling that the physical return has been received and money transfer can
     * proceed.
     */
    public AdminRefundOrderDto completeRefundOrder(Integer refundOrderId) {
        RefundOrder refundOrder = findOrThrow(refundOrderId);

        if (refundOrder.getStatus() != RefundStatus.APPROVED && refundOrder.getStatus() != RefundStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only APPROVED refund orders can be marked as COMPLETED. Current status: "
                            + refundOrder.getStatus());
        }

        refundOrder.setStatus(RefundStatus.DELIVERED);
        RefundOrder saved = refundOrderRepository.save(refundOrder);
        log.info("RefundOrder {} marked COMPLETED by admin", refundOrderId);
        return toDto(saved);
    }

    // -----------------------------------------------------------------------
    // Process money refund → Kafka
    // -----------------------------------------------------------------------

    /**
     * Publishes a {@code process-refund-topic} Kafka event so the User Service can
     * credit the buyer's wallet. Only allowed when status is
     * {@link RefundStatus#DELIVERED}.
     *
     * <p>
     * The status is NOT changed here; it will be updated to
     * {@link RefundStatus#REFUNDED} once the User Service publishes a success
     * acknowledgement on {@code refund-wallet-success-topic}.
     */
    public AdminRefundOrderDto processMoneyRefund(Integer refundOrderId) {
        RefundOrder refundOrder = findOrThrow(refundOrderId);

        if (refundOrder.getStatus() != RefundStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Money refund can only be processed for COMPLETED refund orders. Current status: "
                            + refundOrder.getStatus());
        }

        refundOrder.setStatus(RefundStatus.REFUNDING);
        RefundOrder saved = refundOrderRepository.save(refundOrder);

        ProcessRefundEventDto event = ProcessRefundEventDto.builder()
                .refundOrderId(refundOrder.getId())
                .orderId(refundOrder.getOrderId())
                .userId(refundOrder.getUserId())
                .amount(refundOrder.getTotal())
                .build();

        kafkaEventTemplate.send("process-refund-topic", event);
        log.info("Published process-refund-topic for refundOrderId={}, userId={}, amount={}",
                refundOrder.getId(), refundOrder.getUserId(), refundOrder.getTotal());

        return toDto(saved);
    }

    // -----------------------------------------------------------------------
    // Kafka callback: wallet credited → mark REFUNDED
    // -----------------------------------------------------------------------

    /**
     * Called by {@code KafkaService} when the User Service confirms that the
     * wallet has been successfully credited. Transitions status →
     * {@link RefundStatus#REFUNDED}.
     */
    public void markRefunded(Integer refundOrderId) {
        RefundOrder refundOrder = refundOrderRepository.findById(refundOrderId)
                .orElse(null);

        if (refundOrder == null) {
            log.error("markRefunded: RefundOrder {} not found", refundOrderId);
            return;
        }

        if (refundOrder.getStatus() == RefundStatus.REFUNDED) {
            log.warn("markRefunded: RefundOrder {} is already REFUNDED – skipping duplicate event", refundOrderId);
            return;
        }

        refundOrder.setStatus(RefundStatus.REFUNDED);
        refundOrderRepository.save(refundOrder);
        log.info("RefundOrder {} status updated to REFUNDED", refundOrderId);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private RefundOrder findOrThrow(Integer id) {
        return refundOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Refund order not found: " + id));
    }

    private AdminRefundOrderDto toDto(RefundOrder r) {
        List<AdminRefundOrderDto.AdminRefundItemDto> itemDtos = new ArrayList<>();

        if (r.getOrderItems() != null) {
            itemDtos = r.getOrderItems().stream()
                    .map(item -> AdminRefundOrderDto.AdminRefundItemDto.builder()
                            .orderItemId(item.getId())
                            .productVariantId(item.getProductVariantId())
                            .productVariantName(item.getProductVariantName())
                            .productImageUrl(item.getProductImageUrl())
                            .quantity(item.getQuantity())
                            .unitRefundAmount(item.calculateUnitRefundAmount())
                            .build())
                    .collect(Collectors.toList());
        }

        String uName = orderRepository.findById(r.getOrderId())
                .map(Order::getUserName)
                .orElse(null);

        return AdminRefundOrderDto.builder()
                .refundOrderId(r.getId())
                .orderId(r.getOrderId())
                .userId(r.getUserId())
                .userName(uName)
                .total(r.getTotal())
                .status(r.getStatus())
                .fromAddress(r.getFromAddress())
                .phoneNumber(r.getPhoneNumber())
                .note(r.getNote())
                .createdAt(r.getCreatedAt())
                .evidenceImageUrls(r.getEvidenceImageUrls())
                .items(itemDtos)
                .build();
    }
}
