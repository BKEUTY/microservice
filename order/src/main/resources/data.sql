DELETE FROM order_item;
DELETE FROM orders;
DELETE FROM cart_item;
DELETE FROM payment_transaction;
 
INSERT INTO orders (id, total, payment_method, order_date, address, user_id, status) VALUES
(1, 900000,   'COD',     '2026-03-10', '123 Lý Thường Kiệt, Quận 10, HCM',    'user-1', 4),
(2, 380000,   'Banking', '2026-03-12', '45 Hàng Bài, Hoàn Kiếm, Hà Nội',      'user-2', 4),
(3, 5500000,  'VNPAY',   '2026-03-15', '789 Điện Biên Phủ, Bình Thạnh, HCM',  'user-3', 4),
(4, 1200000,  'COD',     '2026-03-16', '12 Lê Lợi, Quận 1, HCM',              'user-4', 4),
(5, 450000,   'COD',     '2026-03-18', '77 CMT8, Quận 3, HCM',                'user-5', 4),
(6, 350000,   'COD',     '2026-03-20', '123 Lý Thường Kiệt, Quận 10, HCM',    'user-1', 4);

INSERT INTO order_item (id, quantity, product_variant_id, order_id, is_reviewed) VALUES
(1, 1, 1,  1, true),
(2, 1, 2,  2, true),
(3, 1, 1,  3, true),
(4, 1, 1,  6, true),
(5, 1, 1,  4, true),
(6, 1, 1,  5, true),
(7, 1, 1,  1, true),
(8, 1, 1,  2, true),
(9, 1, 2,  4, true);
 
SELECT setval(pg_get_serial_sequence('orders',     'id'), coalesce((SELECT MAX(id) FROM orders),     0) + 1, false);
SELECT setval(pg_get_serial_sequence('order_item', 'id'), coalesce((SELECT MAX(id) FROM order_item), 0) + 1, false);

SELECT setval('payment_transaction_seq', coalesce((SELECT MAX(id) FROM payment_transaction), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('cart_item',  'id'), coalesce((SELECT MAX(id) FROM cart_item),  0) + 1, false);