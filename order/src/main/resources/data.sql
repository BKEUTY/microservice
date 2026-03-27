DELETE FROM order_item;
DELETE FROM orders;
DELETE FROM cart_item;
DELETE FROM payment_transaction;
 
INSERT INTO orders (id, total, payment_method, order_date, address, user_id, status) VALUES
(1, 900000,   'COD',     '2026-03-10', '123 Lý Thường Kiệt, Quận 10, HCM',    'user_001', 0),
(2, 380000,   'Banking', '2026-03-12', '45 Hàng Bài, Hoàn Kiếm, Hà Nội',      'user_002', 1),
(3, 5500000,  'VNPAY',   '2026-03-15', '789 Điện Biên Phủ, Bình Thạnh, HCM',  'user_003', 4),
(4, 1200000,  'COD',     '2026-03-16', '12 Lê Lợi, Quận 1, HCM',              'user_001', 0);
 
INSERT INTO order_item (id, quantity, product_variant_id, order_id) VALUES
(1, 1, 1,  1),
(2, 1, 2,  1),
(3, 1, 4,  2),
(4, 1, 18, 3),
(5, 1, 7,  4),
(6, 1, 5,  4);
 
SELECT setval(pg_get_serial_sequence('orders',     'id'), coalesce((SELECT MAX(id) FROM orders),     0) + 1, false);
SELECT setval(pg_get_serial_sequence('order_item', 'id'), coalesce((SELECT MAX(id) FROM order_item), 0) + 1, false);