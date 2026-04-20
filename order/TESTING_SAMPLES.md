# Hướng dẫn Kiểm thử luồng Đơn hàng (Testing Samples)

Tài liệu này hướng dẫn bạn cách chạy các API để kiểm tra sự thay đổi của 3 trạng thái quan trọng: `status`, `paymentStatus`, và `shippingStatus`.

---

## Các trạng thái cần kiểm tra
1. **`status`**: `NOT_CONFIRMED` -> `CONFIRMED` (sau khi trừ kho).
2. **`paymentStatus`**: `UNPAID` -> `PAID` (sau khi thanh toán).
3. **`shippingStatus`**: `NONE` -> (cập nhật từ GHN sau khi PAID).

---

## Kịch bản 1: Đặt hàng và Xác nhận Kho (NOT_CONFIRMED -> CONFIRMED)

### Bước 1: Đặt hàng
- **API**: `POST /api/order/place-order`
- **Body**:
```json
{
  "paymentMethod": "BANK",
  "address": {
    "address": "123 Đường ABC",
    "ward": { "wardCode": 20109, "wardName": "Phường Thuận Phước" },
    "district": { "districtID": 1444, "districtName": "Quận Hải Châu" },
    "province": { "provinceID": 202, "provinceName": "Đà Nẵng" }
  },
  "phoneNumber": "0905123456",
  "name": "Nguyễn Văn A",
  "orderItems": [{ "cartItemId": 1 }]
}
```
- **Kết quả mong đợi**: Trả về `orderId` (ví dụ: `123`). Lúc này `status` là `NOT_CONFIRMED`.

### Bước 2: Kiểm tra trạng thái sau khi Kafka xử lý
Hệ thống sẽ gửi message sang Product service để trừ kho. Sau vài giây, hãy kiểm tra lại:
- **API**: `GET /api/admin/order/123`
- **Kết quả mong đợi**: 
  - `status`: `CONFIRMED` (Nếu trừ kho thành công).
  - `paymentStatus`: `UNPAID`.
  - `shippingStatus`: `NONE`.

---

## Kịch bản 2: Thanh toán và Tạo đơn giao hàng (UNPAID -> PAID)

### Bước 1: Giả lập Webhook từ SePay
Dùng Postman gửi request này tới Gateway để giả lập việc khách đã chuyển tiền thành công. Nội dung chuyển khoản phải chứa mã đơn hàng (ví dụ: `DH123`).
- **API**: `POST /api/payment/webhook`
- **Headers**: `Authorization: Apikey 9DKDHVTKR4JGQFNIFXLO2NEZMHMSTVKDNIQTTQJ9DRQ0BLPUM4HPEEYHXVIVP5G5`
- **Body**:
```json
{
  "id": 10001,
  "gateway": "VCB",
  "transactionDate": "2024-04-19 10:30:00",
  "accountNumber": "SEPQUANG210804",
  "content": "DH123 thanh toan",
  "transferType": "IN",
  "transferAmount": 550000,
  "description": "Chuyen tien don hang 123"
}
```

### Bước 2: Kiểm tra sự thay đổi trạng thái
Sau khi nhận Webhook, Payment Service gửi Kafka sang Order Service. Order Service cập nhật và tự động gọi Shipping Service.
- **API**: `GET /api/admin/order/123`
- **Kết quả mong đợi**:
  - `paymentStatus`: `PAID`.
  - `shippingCode`: Sẽ xuất hiện mã vận đơn (ví dụ: `GHN123ABC`).
  - `shippingStatus`: Sẽ thay đổi từ `NONE` thành trạng thái từ GHN (ví dụ: `Ready To Pick`).

---

## Kịch bản 3: Kiểm tra Polling từ Frontend
Đây là API mà Frontend dùng để check xem đơn đã thanh toán chưa để tắt màn hình QR.
- **API**: `POST /api/payment/status` (Hoặc `/api/order/payment-status` tùy theo cấu hình Gateway)
- **Body**:
```json
{
  "orderId": 123
}
```
- **Kết quả mong đợi**: Trả về `{ "success": true }` nếu đơn đã là `PAID`.

---

## Các API bổ trợ để Debug
- **Lấy danh sách tỉnh/thành (Shipping Service)**: `GET /api/address/province`
- **Tính phí ship trực tiếp (Shipping Service)**: `POST /api/shipping/fee`
- **Xem Log Kafka**: Bạn có thể theo dõi console của các service `order`, `product`, `payment`, `shipping` để thấy các dòng log `Received message...` khi các service giao tiếp với nhau.
