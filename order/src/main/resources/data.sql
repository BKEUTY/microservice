-- DATA DỰ PHÒNG CHO ORDER SERVICE
-- XÓA SẠCH DATA TRƯỚC KHI THÊM MỚI
DELETE FROM order_item;
DELETE FROM orders;

-- 8. ĐƠN HÀNG (orders)
-- Cấu trúc: id, total, payment_method, order_date, address, user_id (String), status (ordinal)
-- PaymentStatus Status: 0 = UNPAID, 1 = PAID, 2 = CANCELLED, 3 = IN_PROGRESS, 4 = COMPLETED
INSERT INTO orders (id, total, payment_method, order_date, address, user_id, status) VALUES
(1, 900000, 'COD', '2026-03-10', '123 Lý Thường Kiệt, Quận 10, HCM', 'user_001', 0),
(2, 380000, 'Banking', '2026-03-12', '45 Hàng Bài, Hoàn Kiếm, Hà Nội', 'user_002', 1),
(3, 5500000, 'VNPAY', '2026-03-15', '789 Điện Biên Phủ, Bình Thạnh, HCM', 'user_003', 4),
(4, 1200000, 'COD', '2026-03-16', '12 Lê Lợi, Quận 1, HCM', 'user_001', 0)
ON CONFLICT (id) DO NOTHING;

-- 9. CHI TIẾT ĐƠN HÀNG (order_item)
INSERT INTO order_item (id, quantity, product_variant_id, order_id) VALUES
(1, 1, 1, 1), (2, 1, 2, 1), -- Đơn 1 mua 2 chai Cerave (2 size)
(3, 1, 4, 2),              -- Đơn 2 mua Cetaphil 500ml
(4, 1, 18, 3),             -- Đơn 3 mua Foreo Luna 4
(5, 1, 7, 4), (6, 1, 5, 4)  -- Đơn 4 mua Dior Lipglow và Son 3CE
ON CONFLICT (id) DO NOTHING;
