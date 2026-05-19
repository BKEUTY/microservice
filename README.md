# BKEUTY Microservices - Kiến trúc & Hướng dẫn Vận hành / Architecture & Operations Guide

[English](#english) | [Tiếng Việt](#tiếng-việt)

---

<a name="english"></a>
## English - Technical Documentation

### 🌟 Project Overview
BKEUTY is an advanced e-commerce ecosystem built on a high-performance **Microservices Architecture**. It focuses on modularity, high availability, and seamless cross-service communication to support a luxury beauty shopping experience.

### 🏗 Architecture & Design Patterns
- **Microservices Design:** Independent services with specialized domains (Domain-Driven Design).
- **Service Discovery:** Centralized registry using **Netflix Eureka**.
- **API Gateway:** Unified entry point with **Spring Cloud Gateway** for routing and strict internal security (blocks `/api/*/internal/**`).
- **Event-Driven Architecture:** Asynchronous communication powered by **Apache Kafka** (Saga Orchestration for distributed transactions).
- **Distributed Caching:** Optimized performance and state management using **Redis**.
- **Centralized Identity:** Managed by **Keycloak** (OAuth2/OIDC) utilizing Hybrid Storage (SessionStorage & HttpOnly Cookie).

### 🚦 Detailed Startup Sequence & Instructions
To ensure system stability, please follow the startup steps below in the exact order:

#### Step 1: Discovery Server (Service Registry)
*   **Path:** `/microservice/discoveryserver`
*   **Port:** `8761`
*   **Command:** `./mvnw spring-boot:run`
*   **Role:** Manages the registry of all active microservices.

#### Step 2: Keycloak (Identity Provider)
*   **Path:** `/microservice/keycloak-26.2.5`
*   **Port:** `8181`
*   **Command (Windows):** `bin\kc.bat start-dev --http-port 8181`
*   **Command (Linux/macOS):** `bin/kc.sh start-dev --http-port 8181`
*   **Role:** Handles authentication and authorization. Requires realm `bkeuty` setup.

#### Step 3: API Gateway
*   **Path:** `/microservice/gateway`
*   **Port:** `8080`
*   **Command:** `./mvnw spring-boot:run`
*   **Role:** The entry point for all client requests, handling routing, timeout policies, and global security.

#### Step 4: Auth Service
*   **Path:** `/microservice/auth-service`
*   **Port:** `8083`
*   **Command:** `./mvnw spring-boot:run`
*   **Role:** Manages token validation and internal authentication logic.

#### Step 5: Business Microservices (Core Operations)
Run these services after the infrastructure (Steps 1-4) is online:
1.  **Product Service** (Port `8081`): `/microservice/product` - Catalog management, STI Promotion, AI Recommendation (Gemini), Rating sync.
2.  **Order Service** (Port `8082`): `/microservice/order` - Transaction processing, Redis Cart, Voucher Apportionment, Saga Start.
3.  **Promotion Service** (Port `8084`): `/microservice/promotion-service` - Discount logic, Redis Voucher Tracking, Saga Compensation.
4.  **Review Service** (Port `8085`): `/microservice/review-service` - Customer feedback, Two-way interaction.
5.  **User Service** (Port `8086`): `/microservice/user-service` - Profile management acting as Keycloak Proxy.
6.  **Shipping Service** (Port `8087`): `/microservice/shipping-service` - Logistics integration (GHN).
7.  **Payment Service** (Port `8088`): `/microservice/payment-service` - Financial gateway (SePay).
8.  **Chatbot Service** (Port `8089`): `/microservice/chatbot-service` - AI Chatbot utilizing MongoDB.

### ⚙️ Database Configuration
Create the following databases in PostgreSQL (Default Port `5433`):
```sql
CREATE DATABASE "bkeuty-product"; CREATE DATABASE "bkeuty-order";
CREATE DATABASE "bkeuty-promotion"; CREATE DATABASE "bkeuty-review";
CREATE DATABASE "bkeuty-user"; CREATE DATABASE "bkeuty-shipping";
CREATE DATABASE "bkeuty-payment"; CREATE DATABASE "bkeuty-keycloak";
```
*Note: Chatbot Service uses MongoDB.*

### 🧪 Running Unit Tests
To run unit tests for any specific microservice, navigate to the service directory and run:

*   **Run all tests in a service:**
    ```bash
    mvn clean test
    ```
*   **Run a specific test class:**
    ```bash
    mvn test -Dtest="PaymentServiceTest"
    ```
*   **Run multiple specific test classes:**
    ```bash
    mvn test -Dtest="ShippingServiceTest,AddressServiceTest,KafkaServiceTest"
    ```

### 🧪 Running Integration Tests
Integration tests run against isolated H2 memory databases (configured with PostgreSQL compatibility mode) to verify interaction between Web Controllers, Data Repositories, and Business Services.

*   **Run all integration tests in a service:**
    Navigate to the specific microservice folder (e.g., `/microservice/product`) and run:
    ```bash
    mvn clean test
    ```
*   **Run a specific integration test class:**
    ```bash
    mvn test -Dtest="ProductPromotionRepositoryIntegrationTest"
    ```
*   **Run a specific integration test method:**
    ```bash
    mvn test -Dtest="ProductPromotionRepositoryIntegrationTest#findApplicablePromotions_ShouldReturnEligiblePromotions"
    ```

---

<a name="tiếng-việt"></a>
## Tiếng Việt - Tài liệu Kỹ thuật

### 🌟 Tổng quan Dự án
BKEUTY là một hệ sinh thái thương mại điện tử tiên tiến được xây dựng trên nền tảng **Kiến trúc Microservices** hiệu suất cao. Dự án tập trung vào tính module hóa, khả năng sẵn sàng cao và giao tiếp liền mạch giữa các dịch vụ để hỗ trợ trải nghiệm mua sắm mỹ phẩm xa xỉ.

### 🏗 Kiến trúc & Mô hình Thiết kế
- **Microservices Design:** Các dịch vụ độc lập theo từng miền nghiệp vụ (Domain-Driven Design).
- **Service Discovery:** Đăng ký và quản lý dịch vụ tập trung sử dụng **Netflix Eureka**.
- **API Gateway:** Điểm đầu nhận yêu cầu duy nhất với **Spring Cloud Gateway** để điều phối và bảo mật biên nghiêm ngặt (chặn `/api/*/internal/**`).
- **Event-Driven Architecture:** Giao tiếp bất đồng bộ thông qua **Apache Kafka** (Saga Orchestration cho giao dịch phân tán).
- **Distributed Caching:** Tối ưu hiệu năng và trạng thái bằng **Redis**.
- **Centralized Identity:** Quản lý bởi **Keycloak** (OAuth2/OIDC) áp dụng Hybrid Storage (SessionStorage & HttpOnly Cookie).

### 🚦 Quy trình Khởi chạy & Hướng dẫn Chi tiết
Để đảm bảo hệ thống hoạt động ổn định, vui lòng tuân thủ các bước khởi chạy sau theo đúng thứ tự:

#### Bước 1: Discovery Server (Service Registry)
*   **Thư mục:** `/microservice/discoveryserver`
*   **Port:** `8761`
*   **Lệnh chạy:** `./mvnw spring-boot:run`
*   **Vai trò:** Quản lý danh sách các microservices đang hoạt động trong hệ thống.

#### Bước 2: Keycloak (Identity Provider)
*   **Thư mục:** `/microservice/keycloak-26.2.5`
*   **Port:** `8181`
*   **Lệnh chạy (Windows):** `bin\kc.bat start-dev --http-port 8181`
*   **Lệnh chạy (Linux/macOS):** `bin/kc.sh start-dev --http-port 8181`
*   **Vai trò:** Xử lý xác thực và phân quyền. Cần cấu hình realm `bkeuty` và các client ID.

#### Bước 3: API Gateway
*   **Thư mục:** `/microservice/gateway`
*   **Port:** `8080`
*   **Lệnh chạy:** `./mvnw spring-boot:run`
*   **Vai trò:** Cổng vào duy nhất cho mọi request từ Client, điều phối routing, quản lý timeout và bảo mật biên.

#### Bước 4: Auth Service
*   **Thư mục:** `/microservice/auth-service`
*   **Port:** `8083`
*   **Lệnh chạy:** `./mvnw spring-boot:run`
*   **Vai trò:** Xử lý xác thực Token và logic định danh nội bộ.

#### Bước 5: Các Business Microservices (Dịch vụ Nghiệp vụ)
Khởi chạy sau khi hạ tầng (Bước 1-4) đã sẵn sàng:
1.  **Product Service** (Port `8081`): `/microservice/product` - Quản lý sản phẩm, STI Promotion, AI Recommendation (Gemini), đồng bộ đánh giá.
2.  **Order Service** (Port `8082`): `/microservice/order` - Xử lý đơn hàng, Redis Cart, phân bổ Voucher, khởi chạy Saga.
3.  **Promotion Service** (Port `8084`): `/microservice/promotion-service` - Logic khuyến mãi, theo dõi Voucher bằng Redis, Saga Compensation.
4.  **Review Service** (Port `8085`): `/microservice/review-service` - Quản lý đánh giá và phản hồi 2 chiều.
5.  **User Service** (Port `8086`): `/microservice/user-service` - Quản lý hồ sơ, hoạt động như Proxy của Keycloak.
6.  **Shipping Service** (Port `8087`): `/microservice/shipping-service` - Tích hợp vận chuyển thực tế (GHN).
7.  **Payment Service** (Port `8088`): `/microservice/payment-service` - Cổng thanh toán (SePay).
8.  **Chatbot Service** (Port `8089`): `/microservice/chatbot-service` - AI Chatbot lưu trữ bằng MongoDB.

### ⚙️ Cấu hình Cơ sở dữ liệu
Khởi tạo các database sau trong PostgreSQL (Mặc định Port `5433`):
```sql
CREATE DATABASE "bkeuty-product"; CREATE DATABASE "bkeuty-order";
CREATE DATABASE "bkeuty-promotion"; CREATE DATABASE "bkeuty-review";
CREATE DATABASE "bkeuty-user"; CREATE DATABASE "bkeuty-shipping";
CREATE DATABASE "bkeuty-payment"; CREATE DATABASE "bkeuty-keycloak";
```
*Lưu ý: Chatbot Service sử dụng MongoDB.*

### 🧪 Quy trình Chạy Unit Test
Để thực thi các kiểm thử đơn vị (unit test) cho bất kỳ microservice nào, vui lòng di chuyển vào thư mục của dịch vụ đó và chạy các lệnh sau:

*   **Chạy toàn bộ kiểm thử của dịch vụ:**
    ```bash
    mvn clean test
    ```
*   **Chạy một lớp kiểm thử cụ thể:**
    ```bash
    mvn test -Dtest="PaymentServiceTest"
    ```
*   **Chạy nhiều lớp kiểm thử đồng thời:**
    ```bash
    mvn test -Dtest="ShippingServiceTest,AddressServiceTest,KafkaServiceTest"
    ```

### 🧪 Quy trình Chạy Integration Test (Kiểm thử Tích hợp)
Kiểm thử tích hợp hoạt động trên cơ sở dữ liệu H2 bộ nhớ cô lập (ở chế độ tương thích PostgreSQL) nhằm kiểm tra sự phối hợp chính xác giữa tầng Web (Controller), tầng Dữ liệu (Repository) và tầng Nghiệp vụ (Service).

*   **Chạy toàn bộ kiểm thử tích hợp của dịch vụ:**
    Di chuyển vào thư mục dịch vụ cụ thể (ví dụ `/microservice/product`) và chạy:
    ```bash
    mvn clean test
    ```
*   **Chạy một lớp kiểm thử tích hợp cụ thể:**
    ```bash
    mvn test -Dtest="ProductPromotionRepositoryIntegrationTest"
    ```
*   **Chạy một kịch bản kiểm thử tích hợp cụ thể:**
    ```bash
    mvn test -Dtest="ProductPromotionRepositoryIntegrationTest#findApplicablePromotions_ShouldReturnEligiblePromotions"
    ```

---
© 2026 BKEUTY Project. Documentation for Thesis and Academic Research.
