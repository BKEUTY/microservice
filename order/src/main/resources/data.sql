DELETE FROM order_item;
DELETE FROM orders;
DELETE FROM cart_item;
DELETE FROM payment_transaction;

INSERT INTO orders (id, total, payment_method, order_date, address, user_id, status, user_name, shipping_fee, estimated_shipping_date) VALUES
(1, 930000, 'COD', '2026-03-10', '123 Lý Thường Kiệt, Quận 10, HCM|10|1|1', 'user-1', 0, 'Nguyễn Văn A', 30000, '2026-03-12'),
(2, 1105000, 'VNPAY', '2026-03-11', '45 Hàng Bài, Hoàn Kiếm, Hà Nội|2|3|2', 'user-2', 0, 'Trần Thị B', 25000, '2026-03-13'),
(3, 5600000, 'Banking', '2026-03-15', '789 Điện Biên Phủ, Bình Thạnh, HCM|10|1|1', 'user-3', 1, 'Lê Hoàng C', 50000, '2026-03-17'),
(4, 1230000, 'COD', '2026-03-16', '12 Lê Lợi, Quận 1, HCM|1|1|1', 'user-4', 1, 'Phạm Tuấn D', 20000, '2026-03-18'),
(5, 465000, 'VNPAY', '2026-03-18', '77 CMT8, Quận 3, HCM|3|1|1', 'user-5', 3, 'Võ Minh E', 15000, '2026-03-20'),
(6, 880000, 'COD', '2026-03-20', '123 Lý Thường Kiệt, Quận 10, HCM|10|1|1', 'user-1', 3, 'Nguyễn Văn A', 0, '2026-03-22'),
(7, 3535000, 'Banking', '2026-03-22', '55 Trần Hưng Đạo, Quận 5, HCM|5|1|1', 'user-6', 3, 'Bùi Hữu F', 40000, '2026-03-24'),
(8, 3485000, 'COD', '2026-03-25', '88 Hàng Bê, Hoàn Kiếm, Hà Nội|1|3|2', 'user-7', 4, 'Dương Thị G', 35000, '2026-03-27'),
(9, 520000, 'VNPAY', '2026-04-01', '200 Nguyễn Huệ, Quận 1, HCM|1|1|1', 'user-8', 4, 'Hoàng Văn H', 0, '2026-04-03'),
(10, 2595000, 'Banking', '2026-04-03', '66 Bạch Đằng, Hải Phòng|1|4|3', 'user-9', 1, 'Trịnh Thị I', 50000, '2026-04-05'),
(11, 450000, 'COD', '2026-04-05', '99 Lê Thánh Tông, Hoàn Kiếm, Hà Nội|2|3|2', 'user-2', 0, 'Trần Thị B', 0, '2026-04-07'),
(12, 1920000, 'VNPAY', '2026-04-07', '111 Tôn Đức Thắng, Quận 1, HCM|1|1|1', 'user-3', 1, 'Lê Hoàng C', 45000, '2026-04-09'),
(13, 1210000, 'COD', '2026-04-10', '44 Nguyễn Chí Thanh, Ba Đình, Hà Nội|3|3|2', 'user-10', 4, 'Vũ Thị J', 30000, '2026-04-12'),
(14, 2775000, 'Banking', '2026-04-12', '222 Trần Phú, Hà Đông, Hà Nội|5|3|2', 'user-4', 4, 'Phạm Tuấn D', 35000, '2026-04-14'),
(15, 3710000, 'VNPAY', '2026-04-14', '333 Phạm Hùng, Mỹ Đình, Hà Nội|7|3|2', 'user-5', 0, 'Võ Minh E', 55000, '2026-04-16'),
(16, 1215000, 'COD', '2026-04-16', '144 Ba Trieu, Hoan Kiem, Ha Noi|2|3|2', 'user-6', 0, 'Đặng Văn K', 25000, '2026-04-18'),
(17, 1155000, 'Banking', '2026-04-18', '255 Tay Ho, Tay Ho, Ha Noi|6|3|2', 'user-7', 1, 'Hoàng Thị L', 30000, '2026-04-20'),
(18, 2325000, 'VNPAY', '2026-04-20', '366 Dinh Tien Hoang, Hoan Kiem, Ha Noi|1|3|2', 'user-1', 1, 'Nguyễn Văn M', 35000, '2026-04-22'),
(19, 870000, 'COD', '2026-04-22', '477 Ta Hien, Hoan Kiem, Ha Noi|2|3|2', 'user-8', 3, 'Trần Thị N', 10000, '2026-04-24'),
(20, 2320000, 'Banking', '2026-04-24', '588 Hang Gai, Hoan Kiem, Ha Noi|1|3|2', 'user-9', 4, 'Lê Thị O', 40000, '2026-04-26');

INSERT INTO order_item (id, quantity, product_variant_id, order_id, is_reviewed, product_variant_name, product_image_url, price, promotion_price, product_description) VALUES
(1, 1, 1, 1, true, 'CeraVe Foaming Cleanser - 236ml', 'v1.jpg', 350000, 315000, 'Làm sạch sâu da dầu'),
(2, 2, 2, 2, true, 'CeraVe Foaming Cleanser - 473ml', 'v2.jpg', 560000, 520000, 'Dung tích lớn tiết kiệm'),
(3, 1, 6, 3, true, 'Estee Lauder ANR - 30ml', 'v6.jpg', 2150000, 1950000, 'Phục hồi ban đêm'),
(4, 1, 4, 3, false, 'LRP Anthelios SPF 50+ - Mọi loại da', 'v4.jpg', 495000, 445000, 'Chống nắng phổ rộng'),
(5, 1, 1, 4, true, 'CeraVe Foaming Cleanser - 236ml', 'v1.jpg', 350000, 315000, 'Làm sạch sâu da dầu'),
(6, 1, 13, 5, false, '3CE Velvet Lip Tint - Going Right', 'v13.jpg', 380000, 320000, 'Cam hồng đất'),
(7, 1, 9, 6, true, 'Dior Lip Glow - Pink 001', 'v9.jpg', 880000, 880000, 'Hồng tự nhiên'),
(8, 1, 7, 7, false, 'Estee Lauder ANR - 50ml', 'v7.jpg', 3500000, 3100000, 'Dung tích khuyên dùng'),
(9, 1, 17, 7, true, 'Skin1004 Centella Serum - 55ml', 'v17.jpg', 320000, 285000, 'Làm dịu da mụn'),
(10, 1, 25, 8, true, 'Dior Sauvage EDP - 100ml', 'v25.jpg', 3450000, 3150000, 'Bản fullsize'),
(11, 1, 2, 9, false, 'CeraVe Foaming Cleanser - 473ml', 'v2.jpg', 560000, 520000, 'Dung tích lớn tiết kiệm'),
(12, 1, 22, 10, true, 'Kiehl''s Calendula - 250ml', 'v22.jpg', 1250000, 1150000, 'Bản tiêu chuẩn'),
(13, 1, 5, 10, false, 'LRP Anthelios SPF 50+ - Da khô', 'v5.jpg', 495000, 495000, 'Cấp ẩm chống nắng'),
(14, 1, 18, 11, true, 'Skin1004 Centella Serum - 100ml', 'v18.jpg', 495000, 450000, 'Siêu tiết kiệm'),
(15, 1, 20, 12, false, 'Laneige Water Bank - Dạng Cream', 'v20.jpg', 950000, 850000, 'Cho da khô'),
(16, 1, 14, 12, true, '3CE Velvet Lip Tint - Daffodil', 'v14.jpg', 380000, 320000, 'Đỏ đất quyến rũ'),
(17, 1, 27, 13, false, 'Laneige Neo Cushion - 21N', 'v27.jpg', 650000, 580000, 'Tone sáng tự nhiên'),
(18, 1, 10, 14, true, 'Dior Lip Glow - Coral 004', 'v10.jpg', 880000, 880000, 'Cam san hô'),
(19, 1, 19, 14, true, 'Laneige Water Bank - Dạng Gel', 'v19.jpg', 950000, 850000, 'Cho da dầu'),
(20, 1, 24, 15, false, 'Dior Sauvage EDP - 60ml', 'v24.jpg', 2450000, 2450000, 'Nước hoa nam quyến rũ'),
(21, 1, 28, 15, true, 'Laneige Neo Cushion - 23N', 'v28.jpg', 650000, 580000, 'Tone da ngăm nhẹ'),
(22, 1, 3, 16, false, 'CeraVe Foaming Cleanser - 1000ml', 'v3.jpg', 890000, 890000, 'Chai siêu lớn'),
(23, 1, 23, 17, true, 'Kiehl''s Calendula - 500ml', 'v23.jpg', 1950000, 1950000, 'Bản siêu lớn'),
(24, 1, 8, 18, false, 'Estee Lauder ANR - 75ml', 'v8.jpg', 4600000, 4600000, 'Bản giới hạn'),
(25, 1, 15, 19, true, '3CE Velvet Lip Tint - Taupe', 'v15.jpg', 380000, 380000, 'Đỏ nâu gạch số 1'),
(26, 1, 11, 20, false, 'Dior Lip Glow - Berry 006', 'v11.jpg', 880000, 850000, 'Màu mận chín');

SELECT setval(pg_get_serial_sequence('orders', 'id'), coalesce((SELECT MAX(id) FROM orders), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('order_item', 'id'), coalesce((SELECT MAX(id) FROM order_item), 0) + 1, false);
SELECT setval('payment_transaction_seq', coalesce((SELECT MAX(id) FROM payment_transaction), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('cart_item', 'id'), coalesce((SELECT MAX(id) FROM cart_item), 0) + 1, false);