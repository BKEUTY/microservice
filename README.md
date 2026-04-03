# Hướng dẫn cài đặt hệ thống Microservices Bkeuty

Tài liệu này hướng dẫn các bước chi tiết để thiết lập và chạy hệ thống microservices của Bkeuty lần đầu tiên.

## 1. Yêu cầu hệ thống (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt:
- **Java JDK 21** (Yêu cầu bắt buộc cho các project Spring Boot 4.x)
- **Maven 3.9+**
- **PostgreSQL**
- **Git**

## 2. Thiết lập Cơ sở dữ liệu (Database Setup)

Hệ thống sử dụng PostgreSQL với các database riêng biệt cho từng service. Hãy tạo các database sau:

```bash
# Đăng nhập vào PostgreSQL và chạy các lệnh:
CREATE DATABASE "bkeuty-product";
CREATE DATABASE "bkeuty-order";
CREATE DATABASE "bkeuty-promotion";
CREATE DATABASE "bkeuty-review";
CREATE DATABASE "bkeuty-keycloak";
```

**Lưu ý:** Nếu bạn sử dụng port khác của PostgreSQL, hãy cập nhật trong file `application.yaml` của từng service hoặc cấu hình biến môi trường `DATABASE_URL`.

## 3. Cấu hình Biến môi trường (Environment Variables)

Một số service yêu cầu các thông tin cấu hình từ môi trường. Bạn có thể thiết lập các biến này trong hệ thống hoặc file cấu hình IDE:

- **Cloudinary (Dành cho Product Service):**
  - `CLOUDINARY_NAME`
  - `CLOUDINARY_API_KEY`
  - `CLOUDINARY_API_SECRET`
- **Database:**
  - `DATABASE_USERNAME`
  - `DATABASE_PASSWORD`

## 4. Thứ tự khởi chạy (Starting Order)

Để hệ thống hoạt động chính xác, hãy khởi chạy theo thứ tự sau:

### Bước 1: Discovery Server (Service Registry)
- **Thư mục:** `/microservice/discoveryserver`
- **Port:** `8761`
- **Lệnh chạy:** `./mvnw spring-boot:run`
- **Vai trò:** Quản lý danh sách các microservices đang chạy.

### Bước 2: Keycloak (Identity Provider)
- **Thư mục:** `/microservice/keycloak-26.2.5`
- **Port:** `8181`
- **Lệnh chạy (Windows):** `bin\kc.bat start-dev --http-port 8181`
- **Lệnh chạy (Linux/macOS):** `bin/kc.sh start-dev --http-port 8181`
- **Vai trò:** Quản lý Authentication và Authorization. 
- **Setup:** Sau khi chạy, truy cập `http://localhost:8181` để tạo realm `bkeuty` và các client cần thiết.

### Bước 3: API Gateway
- **Thư mục:** `/microservice/gateway`
- **Port:** `8080`
- **Lệnh chạy:** `./mvnw spring-boot:run`
- **Vai trò:** Điểm đầu nhận mọi request từ Client và điều hướng (routing).

### Bước 4: Auth Service
- **Thư mục:** `/microservice/auth-service
- **Port:** `8083`
- **Lệnh chạy:** `./mvnw spring-boot:run`

### Bước 5: Các Business Services (Chạy đồng thời)
- **Product Service:** Port `8081` (Thư mục: `/microservice/product`)
- **Order Service:** Port `8082` (Thư mục: `/microservice/order`)
- **Promotion Service:** Port `8084` (Thư mục: `/microservice/promotion-service`)
- **Review Service:** Port `8085` (Thư mục: `/microservice/review-service`)

## 5. Tổng kết danh sách Port

| Service | Port | Database |
| :--- | :--- | :--- |
| API Gateway | 8080 | - |
| Product Service | 8081 | bkeuty-product |
| Order Service | 8082 | bkeuty-order |
| Auth / User Service | 8083 | - |
| Promotion Service | 8084 | bkeuty-promotion |
| Review Service | 8085 | bkeuty-review |
| Keycloak | 8181 | bkeuty-keycloak |
| Discovery Server | 8761 | - |

## 6. Lưu ý quan trọng
- Kiểm tra file `pom.xml` của từng service để đảm bảo các dependency được tải đủ.
- Nếu gặp lỗi kết nối Database, hãy kiểm tra lại port `5433` và password trong `application.yaml`.
- Các service sử dụng Eureka nên sẽ tự động đăng ký với Discovery Server sau khi khởi chạy hoàn tất.

## 7. Tài liệu API (Swagger UI)

Hệ thống đã được tích hợp Swagger UI tập trung tại API Gateway. Bạn có thể xem tài liệu API của tất cả các services tại một nơi duy nhất.

- **Địa chỉ truy cập:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Cách sử dụng:** 
    1. Truy cập vào link trên.
    2. Ở góc trên bên phải, tìm menu thả xuống **"Select a definition"**.
    3. Chọn service bạn muốn xem tài liệu (ví dụ: `Product Service`, `Order Service`, ...).

**Lưu ý:** Gateway phải đang chạy để có thể truy cập được bảng điều khiển tập trung này.
