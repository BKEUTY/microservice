DELETE FROM variant_option_values;
DELETE FROM product_variant;
DELETE FROM product_option_value;
DELETE FROM product_option;
DELETE FROM product_categories;
DELETE FROM product;
DELETE FROM product_category;
DELETE FROM product_brand;

INSERT INTO product_brand (id, brand_name, description, image, category, brand_status) VALUES
(1, 'CeraVe', 'Thương hiệu dược mỹ phẩm CeraVe', 'cerave_logo.png', 'Chăm sóc da', 0),
(2, 'Cetaphil', 'Chăm sóc da dịu nhẹ', 'cetaphil_logo.png', 'Chăm sóc da', 0),
(3, 'La Roche-Posay', 'Dược mỹ phẩm Pháp', 'lrp_logo.png', 'Chăm sóc da', 0),
(4, '3CE', 'Thương hiệu mỹ phẩm Hàn Quốc', '3ce_logo.png', 'Trang điểm', 0),
(5, 'Dior', 'Thương hiệu cao cấp', 'dior_logo.png', 'Trang điểm', 0),
(6, 'Estee Lauder', 'Chăm sóc da cao cấp', 'estee_logo.png', 'Chăm sóc da', 0),
(7, 'Khác', 'Các thương hiệu khác', 'default_brand.png', 'Khác', 0);

INSERT INTO product_category (id, category_name) VALUES
(1, 'Chăm sóc da'), (2, 'Trang điểm'), (3, 'Nước hoa'),
(4, 'Chăm sóc tóc'), (5, 'Dụng cụ làm đẹp'), (6, 'Bộ Quà Tặng'),
(7, 'Sữa rửa mặt'), (8, 'Nước cân bằng'), (9, 'Tinh chất'),
(10, 'Kem dưỡng'), (11, 'Chống nắng'), (12, 'Trang điểm mặt'),
(13, 'Trang điểm môi'), (14, 'Trang điểm mắt');

INSERT INTO product (id, name, description, image, product_brand) VALUES
(1, 'Sữa Rửa Mặt CeraVe Foaming Cleanser', 'Sữa rửa mặt tạo bọt làm sạch sâu cho da dầu.', NULL, 1),
(2, 'Sữa Rửa Mặt Cetaphil Gentle Skin Cleanser', 'Công thức dịu nhẹ không gây kích ứng.', NULL, 2),
(3, 'Sữa Rửa Mặt La Roche-Posay Effaclar', 'Gel rửa mặt cho da dầu nhạy cảm.', NULL, 3),
(4, 'Sữa Rửa Mặt Cosrx Low pH Good Morning', 'Cân bằng độ pH tự nhiên cho da.', NULL, 7),
(5, 'Sữa Rửa Mặt Innisfree Green Tea', 'Chiết xuất trà xanh giúp sạch sâu.', NULL, 7),
(6, 'Kem Chống Nắng Anessa Perfect UV', 'Công nghệ Aqua Booster chống nước mạnh mẽ.', NULL, 7),
(7, 'Kem Chống Nắng La Roche-Posay Anthelios', 'Khả năng chống nắng quang phổ rộng.', NULL, 3),
(8, 'Kem Chống Nắng Skin1004 Madagascar', 'Chiết xuất rau má làm dịu da.', NULL, 7),
(9, 'Kem Chống Nắng Cell Fusion C Laser', 'Dành cho da sau điều trị laser.', NULL, 7),
(10, 'Kem Chống Nắng L''Oreal Invisible Fluid', 'Kết cấu mỏng nhẹ không để lại vệt trắng.', NULL, 7),
(11, 'Nước Hoa Hồng Lancôme Tonique Confort', 'Dưỡng ẩm sâu cho da khô.', NULL, 7),
(12, 'Toner Kiehl''s Calendula Herbal', 'Chiết xuất hoa cúc làm dịu da.', NULL, 7),
(13, 'Toner Mamonde Rose Water', '90.97% tinh chất hoa hồng.', NULL, 7),
(14, 'Nước Hoa Hồng Klairs Supple Preparation', 'Cấp ẩm tức thì cho da.', NULL, 7),
(15, 'Toner Paula''s Choice Skin Balancing', 'Se khít lỗ chân lông.', NULL, 7),
(16, 'Serum Estee Lauder Advanced Night Repair', 'Phục hồi da ban đêm thần thánh.', NULL, 6),
(17, 'Serum The Ordinary Niacinamide 10% + Zinc 1', 'Giảm thâm mụn và thu nhỏ lỗ chân lông.', NULL, 7),
(18, 'Serum Skin1004 Madagascar Centella', '100% chiết xuất rau má tinh khiết.', NULL, 7),
(19, 'Serum Klairs Freshly Juiced Vitamin C', 'Làm sáng da và mờ thâm.', NULL, 7),
(20, 'Serum Timeless Vitamin B5 + Hyaluronic Acid', 'Cấp ẩm và phục hồi da tổn thương.', NULL, 7),
(21, 'Kem Dưỡng Neutrogena Hydro Boost Water Gel', 'Cấp ẩm chuyên sâu dạng gel.', NULL, 7),
(22, 'Kem Dưỡng Kiehl''s Ultra Facial Cream', 'Dưỡng ẩm suốt 24 giờ.', NULL, 7),
(23, 'Kem Dưỡng Phục Hồi La Roche-Posay Cicaplast B5', 'Hỗ trợ tái tạo màng bảo vệ da.', NULL, 3),
(24, 'Kem Dưỡng Clinique Dramatically Different', 'Cung cấp độ ẩm tối ưu.', NULL, 7),
(25, 'Kem Dưỡng Laneige Water Bank Blue HA', 'Công nghệ HA mới dưỡng ẩm sâu.', NULL, 7),
(26, 'Phấn Nước Laneige Neo Cushion Matte', 'Che phủ cao và lâu trôi.', NULL, 7),
(27, 'Kem Nền Estee Lauder Double Wear', 'Lớp nền hoàn hảo trứ danh.', NULL, 6),
(28, 'Phấn Phủ Mac Studio Fix Powder Plus', 'Kết hợp nền và phấn 2 trong 1.', NULL, 7),
(29, 'Cushion Hera Black Cushion', 'Đẳng cấp trang điểm Hàn Quốc.', NULL, 7),
(30, 'Kem Nền Maybelline Fit Me', 'Kiềm dầu hiệu quả.', NULL, 7),
(31, 'Son Thỏi 3CE Velvet Lip Tint', 'Màu sắc trẻ trung, chất son mịn.', NULL, 4),
(32, 'Son Kem Romand Juicy Lasting Tint', 'Hiệu ứng căng bóng môi.', NULL, 7),
(33, 'Son Dưỡng Dior Addict Lip Glow', 'Son dưỡng huyền thoại biến hóa màu sắc.', NULL, 5),
(34, 'Son Thỏi MAC Matte Lipstick', 'Màu son chuẩn, lâu trôi.', NULL, 7),
(35, 'Son Kem Black Rouge Air Fit Velvet Tint', 'Mềm lòng với bảng màu đa dạng.', NULL, 7),
(36, 'Nước Hoa Dior Sauvage Eau De Parfum', 'Mạnh mẽ, nam tính.', NULL, 5),
(37, 'Nước Hoa Chanel Coco Mademoiselle', 'Quyến rũ và sang trọng.', NULL, 7),
(38, 'Bộ Quà Tặng Skincare L''Oreal Paris', 'Sự kết hợp hoàn hảo cho làn da.', NULL, 7),
(39, 'Dụng Cụ Massage Nâng Cơ ReFa Carat', 'Công nghệ microcurrent tiên tiến.', NULL, 7),
(40, 'Máy Rửa Mặt Foreo Luna 4', 'Làm sạch sâu gấp 35 lần.', NULL, 7);

INSERT INTO product_categories (product_id, category_id) VALUES
(1, 1), (1, 7), (2, 1), (2, 7), (3, 1), (3, 7), (4, 1), (4, 7), (5, 1), (5, 7),
(6, 1), (6, 11), (7, 1), (7, 11), (8, 1), (8, 11), (9, 1), (9, 11), (10, 1), (10, 11),
(11, 1), (11, 8), (12, 1), (12, 8), (13, 1), (13, 8), (14, 1), (14, 8), (15, 1), (15, 8),
(16, 1), (16, 9), (17, 1), (17, 9), (18, 1), (18, 9), (19, 1), (19, 9), (20, 1), (20, 9),
(21, 1), (21, 10), (22, 1), (22, 10), (23, 1), (23, 10), (24, 1), (24, 10), (25, 1), (25, 10),
(26, 2), (26, 12), (27, 2), (27, 12), (28, 2), (28, 12), (29, 2), (29, 12), (30, 2), (30, 12),
(31, 2), (31, 13), (32, 2), (32, 13), (33, 2), (33, 13), (34, 2), (34, 13), (35, 2), (35, 13),
(36, 3), (37, 3), (38, 6), (39, 5), (40, 5);

INSERT INTO product_option (id, option_name, product_id, status) VALUES
(1, 'Dung tích', 1, 0), (2, 'Dung tích', 2, 0), (3, 'Dung tích', 3, 0),
(4, 'Dung tích', 6, 0), (5, 'Màu sắc', 31, 0), (6, 'Màu sắc', 32, 0),
(7, 'Màu sắc', 33, 0), (8, 'Loại da', 21, 0), (9, 'Dung tích', 16, 0),
(10, 'Màu sắc', 26, 0);

INSERT INTO product_option_value (id, option_value_name, option_id, status) VALUES
(1, '236ml', 1, 0), (2, '473ml', 1, 0),
(3, '125ml', 2, 0), (4, '500ml', 2, 0),
(5, '200ml', 3, 0), (6, '400ml', 3, 0),
(7, '20ml', 4, 0), (8, '60ml', 4, 0),
(9, 'Màu 126', 5, 0), (10, 'Màu 114', 5, 0),
(11, 'Juicy 01', 6, 0), (12, 'Juicy 06', 6, 0),
(13, 'Pink 001', 7, 0), (14, 'Coral 004', 7, 0),
(15, 'Da dầu', 8, 0), (16, 'Da khô', 8, 0),
(17, '30ml', 9, 0), (18, '50ml', 9, 0),
(19, 'Tone 21N', 10, 0), (20, 'Tone 23N', 10, 0);

INSERT INTO product_variant (id, product_id, product_variant_name, price, stock_quantity, description, status, product_image_url, average_rating, review_count) VALUES
(1, 1, 'Sữa Rửa Mặt CeraVe Foaming Cleanser - 236ml', 350000, 100, 'Chai nhỏ tiện lợi', 0, NULL, 4.3, 7),
(2, 1, 'Sữa Rửa Mặt CeraVe Foaming Cleanser - 473ml', 550000, 50, 'Tiết kiệm hơn', 0, NULL, 4.5, 2),
(3, 2, 'Sữa Rửa Mặt Cetaphil Gentle Skin Cleanser - 125ml', 150000, 200, NULL, 0, NULL, 0.0, 0),
(4, 2, 'Sữa Rửa Mặt Cetaphil Gentle Skin Cleanser - 500ml', 380000, 150, NULL, 0, NULL, 0.0, 0),
(5, 31, 'Son Thỏi 3CE Velvet Lip Tint - Màu 126', 380000, 80, 'Màu đỏ gạch', 0, NULL, 0.0, 0),
(6, 31, 'Son Thỏi 3CE Velvet Lip Tint - Màu 114', 380000, 60, 'Màu cam đào', 0, NULL, 0.0, 0),
(7, 33, 'Son Dưỡng Dior Addict Lip Glow - Pink 001', 850000, 40, 'Hồng tự nhiên', 0, NULL, 0.0, 0),
(8, 33, 'Son Dưỡng Dior Addict Lip Glow - Coral 004', 850000, 30, 'Cam san hô', 0, NULL, 0.0, 0),
(9, 16, 'Serum Estee Lauder Advanced Night Repair - 30ml', 2100000, 20, NULL, 0, NULL, 0.0, 0),
(10, 16, 'Serum Estee Lauder Advanced Night Repair - 50ml', 3400000, 15, NULL, 0, NULL, 0.0, 0),
(11, 21, 'Kem Dưỡng Neutrogena Hydro Boost Water Gel - Da dầu', 450000, 120, 'Cực kỳ mỏng nhẹ', 0, NULL, 0.0, 0),
(12, 6, 'Kem Chống Nắng Anessa Perfect UV - 20ml', 250000, 300, 'Bản mini', 0, NULL, 0.0, 0),
(13, 3, 'Sữa Rửa Mặt La Roche-Posay Effaclar - 200ml', 420000, 100, NULL, 0, NULL, 0.0, 0),
(14, 4, 'Sữa Rửa Mặt Cosrx Low pH Good Morning', 280000, 150, NULL, 0, NULL, 0.0, 0),
(15, 7, 'Kem Chống Nắng La Roche-Posay Anthelios', 490000, 80, NULL, 0, NULL, 0.0, 0),
(16, 8, 'Kem Chống Nắng Skin1004 Madagascar', 320000, 90, NULL, 0, NULL, 0.0, 0),
(17, 36, 'Nước Hoa Dior Sauvage Eau De Parfum', 3200000, 25, NULL, 0, NULL, 0.0, 0),
(18, 40, 'Máy Rửa Mặt Foreo Luna 4', 5500000, 10, NULL, 0, NULL, 0.0, 0),
(19, 12, 'Toner Kiehl''s Calendula Herbal', 1100000, 40, NULL, 0, NULL, 0.0, 0),
(20, 22, 'Kem Dưỡng Kiehl''s Ultra Facial Cream', 950000, 50, NULL, 0, NULL, 0.0, 0),
(21, 3, 'Sữa Rửa Mặt La Roche-Posay Effaclar - 400ml', 550000, 80, NULL, 0, NULL, 0.0, 0),
(22, 6, 'Kem Chống Nắng Anessa Perfect UV - 60ml', 650000, 150, 'Bản fullsize', 0, NULL, 0.0, 0),
(23, 32, 'Son Kem Romand Juicy Lasting Tint - Juicy 01', 180000, 100, NULL, 0, NULL, 0.0, 0),
(24, 32, 'Son Kem Romand Juicy Lasting Tint - Juicy 06', 180000, 120, NULL, 0, NULL, 0.0, 0),
(25, 21, 'Kem Dưỡng Neutrogena Hydro Boost Water Gel - Da khô', 450000, 90, 'Chất kem cream ẩm hơn', 0, NULL, 0.0, 0),
(26, 26, 'Phấn Nước Laneige Neo Cushion Matte - Tone 21N', 650000, 60, 'Tone sáng', 0, NULL, 0.0, 0),
(27, 26, 'Phấn Nước Laneige Neo Cushion Matte - Tone 23N', 650000, 50, 'Tone tự nhiên', 0, NULL, 0.0, 0);

INSERT INTO variant_option_values (variant_id, option_value_id) VALUES
(1, 1), (2, 2),
(3, 3), (4, 4),
(13, 5), (21, 6),
(12, 7), (22, 8),
(5, 9), (6, 10),
(23, 11), (24, 12),
(7, 13), (8, 14),
(11, 15), (25, 16),
(9, 17), (10, 18),
(26, 19), (27, 20);

SELECT setval('product_brand_seq', coalesce((SELECT MAX(id) FROM product_brand), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_category',     'id'), coalesce((SELECT MAX(id) FROM product_category),     0) + 1, false);
SELECT setval(pg_get_serial_sequence('product',              'id'), coalesce((SELECT MAX(id) FROM product),              0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_option',       'id'), coalesce((SELECT MAX(id) FROM product_option),       0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_option_value', 'id'), coalesce((SELECT MAX(id) FROM product_option_value), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_variant',      'id'), coalesce((SELECT MAX(id) FROM product_variant),      0) + 1, false);