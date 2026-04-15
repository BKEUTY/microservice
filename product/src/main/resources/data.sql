DELETE FROM variant_option_values;
DELETE FROM product_variant;
DELETE FROM product_option_value;
DELETE FROM product_option;
DELETE FROM product_categories;
DELETE FROM product;
DELETE FROM product_category;
DELETE FROM product_brand;

INSERT INTO product_brand (id, brand_name, description, image, category, brand_status) VALUES
(1, 'CeraVe', 'Thương hiệu dược mỹ phẩm CeraVe', 'cerave_logo.png', 'Skincare', 0),
(2, 'La Roche-Posay', 'Dược mỹ phẩm Pháp cho da nhạy cảm', 'lrp_logo.png', 'Skincare', 0),
(3, 'Estee Lauder', 'Chăm sóc da và trang điểm cao cấp', 'estee_logo.png', 'Beauty', 0),
(4, 'Dior', 'Hãng thời trang và mỹ phẩm xa xỉ', 'dior_logo.png', 'Luxury', 0),
(5, '3CE', 'Mỹ phẩm phong cách trẻ trung Hàn Quốc', '3ce_logo.png', 'Makeup', 0),
(6, 'Skin1004', 'Chiết xuất rau má Madagascar', 'skin1004_logo.png', 'Skincare', 0),
(7, 'Laneige', 'Chuyên gia dưỡng ẩm từ nước', 'laneige_logo.png', 'Skincare', 0),
(8, 'Kiehl''s', 'Thương hiệu dược mỹ phẩm từ New York', 'kiehls_logo.png', 'Skincare', 0);

INSERT INTO product_category (id, category_name) VALUES
(1, 'Skincare'), (2, 'Makeup'), (3, 'Fragrance'), (4, 'Haircare'), 
(5, 'Sữa rửa mặt'), (6, 'Toner'), (7, 'Serum'), (8, 'Kem dưỡng'), (9, 'Chống nắng'),
(10, 'Son môi'), (11, 'Phấn nước (Cushion)'), (12, 'Nước hoa nam'), (13, 'Nước hoa nữ');

INSERT INTO product (id, name, description, image, product_brand) VALUES
(1, 'Sữa Rửa Mặt CeraVe Foaming Cleanser', 'Sữa rửa mặt tạo bọt làm sạch sâu cho da dầu.', 'cerave_foaming.jpg', 1),
(2, 'Kem Chống Nắng La Roche-Posay Anthelios', 'Khả năng chống nắng quang phổ rộng SPF 50+.', 'lrp_anthelios.jpg', 2),
(3, 'Serum Estee Lauder Advanced Night Repair', 'Serum phục hồi da ban đêm số 1.', 'estee_anr.jpg', 3),
(4, 'Son Dưỡng Dior Addict Lip Glow', 'Son dưỡng huyền thoại biến hóa màu sắc.', 'dior_lipglow.jpg', 4),
(5, 'Son Thỏi 3CE Velvet Lip Tint', 'Màu sắc trẻ trung, chất son mịn mượt.', '3ce_velvet.jpg', 5),
(6, 'Serum Skin1004 Madagascar Centella', '100% chiết xuất rau má Madagascar làm dịu da.', 'skin1004_serum.jpg', 6),
(7, 'Kem Dưỡng Laneige Water Bank Blue HA', 'Công nghệ HA mới dưỡng ẩm sâu 100 giờ.', 'laneige_waterbank.jpg', 7),
(8, 'Toner Kiehl''s Calendula Herbal', 'Chiết xuất hoa cúc làm dịu và cân bằng da.', 'kiehls_toner.jpg', 8),
(9, 'Nước Hoa Dior Sauvage EDP', 'Mạnh mẽ, nam tính và lôi cuốn.', 'dior_sauvage.jpg', 4),
(10, 'Phấn Nước Laneige Neo Cushion Matte', 'Che phủ cao, kiềm dầu và lâu trôi.', 'laneige_neo.jpg', 7),
(11, 'Sữa Rửa Mặt La Roche-Posay Effaclar', 'Gel rửa mặt cho da dầu nhạy cảm.', 'lrp_effaclar.jpg', 2),
(12, 'Kem Dưỡng Phục Hồi Cicaplast B5', 'Hỗ trợ tái tạo và làm dịu da kích ứng.', 'lrp_b5.jpg', 2),
(13, 'Son Kem Romand Juicy Lasting Tint', 'Hiệu ứng căng bóng môi như trái cây.', 'romand_juicy.jpg', 8),
(14, 'Nước Hoa Hồng Klairs Supple Preparation', 'Cấp ẩm tức thì và cân bằng pH.', 'klairs_toner.jpg', 8),
(15, 'Kem Chống Nắng Skin1004 Madagascar Centella', 'Chống nắng vật lý nhẹ dịu trên da.', 'skin1004_sun.jpg', 6);

INSERT INTO product_categories (product_id, category_id) VALUES
(1, 1), (1, 5), (2, 1), (2, 9), (3, 1), (3, 7), (4, 2), (4, 10), 
(5, 2), (5, 10), (6, 1), (6, 7), (7, 1), (7, 8), (8, 1), (8, 6),
(9, 3), (9, 12), (10, 2), (10, 11), (11, 1), (11, 5), (12, 1), (12, 8),
(13, 2), (13, 10), (14, 1), (14, 6), (15, 1), (15, 9);

INSERT INTO product_option (id, option_name, product_id, status) VALUES
(1, 'Dung tích', 1, 0), (2, 'Loại da', 2, 0), (3, 'Dung tích', 3, 0), (4, 'Màu sắc', 4, 0),
(5, 'Màu sắc', 5, 0), (6, 'Dung tích', 6, 0), (7, 'Loại da', 7, 0), (8, 'Dung tích', 8, 0),
(9, 'Dung tích', 9, 0), (10, 'Tone màu', 10, 0), (11, 'Dung tích', 11, 0), (12, 'Dung tích', 12, 0),
(13, 'Màu sắc', 13, 0), (14, 'Phiên bản', 14, 0), (15, 'Dung tích', 15, 0);

INSERT INTO product_option_value (id, option_value_name, option_id, status) VALUES
(1, '236ml', 1, 0), (2, '473ml', 1, 0), (3, '1000ml', 1, 0),
(4, 'Mọi loại da', 2, 0), (5, 'Da khô', 2, 0),
(6, '30ml', 3, 0), (7, '50ml', 3, 0), (8, '75ml', 3, 0),
(9, 'Pink 001', 4, 0), (10, 'Coral 004', 4, 0), (11, 'Berry 006', 4, 0), (12, 'Mahogany 030', 4, 0),
(13, 'Going Right', 5, 0), (14, 'Daffodil', 5, 0), (15, 'Taupe', 5, 0), (16, 'Pink Break', 5, 0),
(17, '55ml', 6, 0), (18, '100ml', 6, 0),
(19, 'Dạng Gel', 7, 0), (20, 'Dạng Cream', 7, 0),
(21, '125ml', 8, 0), (22, '250ml', 8, 0), (23, '500ml', 8, 0),
(24, '60ml', 9, 0), (25, '100ml', 9, 0), (26, '200ml', 9, 0),
(27, 'Tone 21N', 10, 0), (28, 'Tone 23N', 10, 0), (29, 'Tone 25N', 10, 0),
(30, '200ml', 11, 0), (31, '400ml', 11, 0),
(32, '40ml', 12, 0), (33, '100ml', 12, 0),
(34, 'Fig Fig', 13, 0), (35, 'Jujube', 13, 0), (36, 'Dark Coconut', 13, 0), (37, 'Peeling Angdoo', 13, 0),
(38, 'Có mùi', 14, 0), (39, 'Không mùi', 14, 0),
(40, '50ml', 15, 0);

INSERT INTO product_variant (id, product_id, product_variant_name, price, promotion_price, stock_quantity, sold, description, status, product_image_url, average_rating, review_count) VALUES
(1, 1, 'CeraVe Foaming Cleanser - 236ml', 350000, 315000, 120, 850, 'Làm sạch sâu da dầu', 0, 'v1.jpg', 4.8, 120),
(2, 1, 'CeraVe Foaming Cleanser - 473ml', 560000, 520000, 80, 1200, 'Dung tích lớn tiết kiệm', 0, 'v2.jpg', 4.9, 250),
(3, 1, 'CeraVe Foaming Cleanser - 1000ml', 890000, 890000, 45, 340, 'Chai siêu lớn', 0, 'v3.jpg', 4.7, 50),
(4, 2, 'LRP Anthelios SPF 50+ - Mọi loại da', 495000, 445000, 200, 1450, 'Chống nắng phổ rộng', 0, 'v4.jpg', 4.9, 980),
(5, 2, 'LRP Anthelios SPF 50+ - Da khô', 495000, 495000, 60, 210, 'Cấp ẩm chống nắng', 0, 'v5.jpg', 4.6, 45),
(6, 3, 'Estee Lauder ANR - 30ml', 2150000, 1950000, 50, 670, 'Phục hồi ban đêm', 0, 'v6.jpg', 5.0, 320),
(7, 3, 'Estee Lauder ANR - 50ml', 3500000, 3100000, 30, 480, 'Dung tích khuyên dùng', 0, 'v7.jpg', 4.9, 150),
(8, 3, 'Estee Lauder ANR - 75ml', 4600000, 4600000, 15, 120, 'Bản giới hạn', 0, 'v8.jpg', 4.8, 20),
(9, 4, 'Dior Lip Glow - Pink 001', 880000, 880000, 100, 1340, 'Hồng tự nhiên', 0, 'v9.jpg', 4.9, 560),
(10, 4, 'Dior Lip Glow - Coral 004', 880000, 880000, 80, 920, 'Cam san hô', 0, 'v10.jpg', 4.8, 340),
(11, 4, 'Dior Lip Glow - Berry 006', 880000, 850000, 40, 450, 'Màu mận chín', 0, 'v11.jpg', 4.7, 120),
(12, 4, 'Dior Lip Glow - Mahogany 030', 880000, 880000, 20, 180, 'Đỏ nâu quyến rũ', 0, 'v12.jpg', 4.9, 80),
(13, 5, '3CE Velvet Lip Tint - Going Right', 380000, 320000, 150, 1100, 'Cam hồng đất', 0, 'v13.jpg', 4.7, 450),
(14, 5, '3CE Velvet Lip Tint - Daffodil', 380000, 320000, 120, 860, 'Đỏ đất quyến rũ', 0, 'v14.jpg', 4.8, 320),
(15, 5, '3CE Velvet Lip Tint - Taupe', 380000, 380000, 90, 1420, 'Đỏ nâu gạch số 1', 0, 'v15.jpg', 4.9, 780),
(16, 5, '3CE Velvet Lip Tint - Pink Break', 380000, 320000, 50, 430, 'Hồng đào ấm', 0, 'v16.jpg', 4.5, 150),
(17, 6, 'Skin1004 Centella Serum - 55ml', 320000, 285000, 250, 1280, 'Làm dịu da mụn', 0, 'v17.jpg', 4.8, 640),
(18, 6, 'Skin1004 Centella Serum - 100ml', 495000, 450000, 180, 950, 'Siêu tiết kiệm', 0, 'v18.jpg', 4.9, 430),
(19, 7, 'Laneige Water Bank - Dạng Gel', 950000, 850000, 100, 560, 'Cho da dầu', 0, 'v19.jpg', 4.7, 180),
(20, 7, 'Laneige Water Bank - Dạng Cream', 950000, 850000, 90, 420, 'Cho da khô', 0, 'v20.jpg', 4.8, 140),
(21, 8, 'Kiehl''s Calendula - 125ml', 750000, 750000, 80, 490, 'Toner hoa cúc nhỏ', 0, 'v21.jpg', 4.8, 210),
(22, 8, 'Kiehl''s Calendula - 250ml', 1250000, 1150000, 60, 680, 'Bản tiêu chuẩn', 0, 'v22.jpg', 4.9, 320),
(23, 8, 'Kiehl''s Calendula - 500ml', 1950000, 1950000, 30, 240, 'Bản siêu lớn', 0, 'v23.jpg', 4.9, 90),
(24, 9, 'Dior Sauvage EDP - 60ml', 2450000, 2450000, 40, 380, 'Nước hoa nam quyến rũ', 0, 'v24.jpg', 4.9, 140),
(25, 9, 'Dior Sauvage EDP - 100ml', 3450000, 3150000, 30, 520, 'Bản fullsize', 0, 'v25.jpg', 5.0, 230),
(26, 9, 'Dior Sauvage EDP - 200ml', 5250000, 5250000, 10, 80, 'Siêu lớn giới hạn', 0, 'v26.jpg', 5.0, 45),
(27, 10, 'Laneige Neo Cushion - 21N', 650000, 580000, 120, 750, 'Tone sáng tự nhiên', 0, 'v27.jpg', 4.8, 280),
(28, 10, 'Laneige Neo Cushion - 23N', 650000, 580000, 100, 640, 'Tone da ngăm nhẹ', 0, 'v28.jpg', 4.7, 210),
(29, 10, 'Laneige Neo Cushion - 25N', 650000, 650000, 40, 120, 'Tone đậm', 0, 'v29.jpg', 4.6, 35),
(30, 11, 'LRP Effaclar Gel - 200ml', 395000, 355000, 150, 1380, 'Sữa rửa mặt da dầu', 0, 'v30.jpg', 4.8, 720),
(31, 11, 'LRP Effaclar Gel - 400ml', 565000, 525000, 120, 1480, 'Chai lớn có vòi nhấn', 0, 'v31.jpg', 4.9, 890),
(32, 12, 'LRP Cicaplast B5 - 40ml', 365000, 335000, 300, 1490, 'Kem phục hồi cấp tốc', 0, 'v32.jpg', 4.9, 1100),
(33, 12, 'LRP Cicaplast B5 - 100ml', 625000, 585000, 150, 820, 'Tuýp lớn tiết kiệm', 0, 'v33.jpg', 4.9, 450),
(34, 13, 'Romand Juicy Tint - Fig Fig', 195000, 175000, 200, 1240, 'Màu tím quả sung', 0, 'v34.jpg', 4.8, 540),
(35, 13, 'Romand Juicy Tint - Jujube', 195000, 175000, 180, 1150, 'Màu đỏ hồng đất', 0, 'v35.jpg', 4.8, 480),
(36, 13, 'Romand Juicy Tint - Dark Coconut', 195000, 175000, 160, 1410, 'Màu đỏ nâu dừa', 0, 'v36.jpg', 4.9, 820),
(37, 13, 'Romand Juicy Tint - Peeling Angdoo', 195000, 195000, 100, 560, 'Màu đỏ đào', 0, 'v37.jpg', 4.7, 150),
(38, 14, 'Klairs Toner - Có mùi', 345000, 310000, 200, 1350, 'Hương thảo mộc', 0, 'v38.jpg', 4.9, 680),
(39, 14, 'Klairs Toner - Không mùi', 345000, 310000, 250, 1495, 'Cho da siêu nhạy cảm', 0, 'v39.jpg', 4.9, 940),
(40, 15, 'Skin1004 Sun Physcial - 50ml', 385000, 345000, 150, 890, 'Chống nắng rau má', 0, 'v40.jpg', 4.8, 380),
(41, 1, 'CeraVe Foaming Cleanser mini - 88ml', 185000, 185000, 200, 450, 'Bản du lịch', 0, 'v41.jpg', 4.7, 120),
(42, 2, 'LRP Anthelios Fluid - 50ml', 525000, 485000, 150, 1150, 'Dạng sữa mỏng nhẹ', 0, 'v42.jpg', 4.9, 560),
(43, 3, 'Estee Lauder ANR mini - 7ml', 350000, 350000, 100, 890, 'Dùng thử 7 ngày', 0, 'v43.jpg', 4.8, 230),
(44, 4, 'Dior Lip Maximizer - Pink 001', 950000, 950000, 50, 680, 'Son bóng dưỡng môi', 0, 'v44.jpg', 4.9, 310),
(45, 5, '3CE Cloud Lip Tint - Live Little', 395000, 350000, 100, 540, 'Chất son xốp mịn', 0, 'v45.jpg', 4.7, 180),
(46, 6, 'Skin1004 Tea-Trica Ampoule', 350000, 310000, 120, 620, 'Cho da dầu mụn', 0, 'v46.jpg', 4.8, 240),
(47, 7, 'Laneige Lip Sleeping Mask', 450000, 410000, 300, 1500, 'Mặt nạ ngủ cho môi', 0, 'v47.jpg', 4.9, 1200),
(48, 8, 'Kiehl''s Rare Earth Mask', 850000, 790000, 90, 740, 'Mặt nạ đất sét se lỗ chân lông', 0, 'v48.jpg', 4.8, 350),
(49, 9, 'Dior J''adore EDP - 50ml', 3250000, 3250000, 30, 410, 'Huyền thoại nước hoa nữ', 0, 'v49.jpg', 5.0, 190),
(50, 10, 'Laneige Glowy Makeup Serum', 650000, 590000, 80, 320, 'Tinh chất lót trang điểm', 0, 'v50.jpg', 4.8, 110),
(51, 11, 'LRP Effaclar Micro-Peeling', 535000, 495000, 100, 680, 'Gel tắm & rửa mặt giảm mụn', 0, 'v51.jpg', 4.9, 210),
(52, 12, 'LRP Cicaplast Baume B5+ SPF50', 450000, 450000, 80, 230, 'Phục hồi & Chống nắng 2in1', 0, 'v52.jpg', 4.7, 90);

INSERT INTO variant_option_values (variant_id, option_value_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8), (9, 9), (10, 10),
(11, 11), (12, 12), (13, 13), (14, 14), (15, 15), (16, 16), (17, 17), (18, 18), (19, 19), (20, 20),
(21, 21), (22, 22), (23, 23), (24, 24), (25, 25), (26, 26), (27, 27), (28, 28), (29, 29), (30, 30),
(31, 31), (32, 32), (33, 33), (34, 34), (35, 35), (36, 36), (37, 37), (38, 38), (39, 39), (40, 40);

SELECT setval('product_brand_seq', coalesce((SELECT MAX(id) FROM product_brand), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_category',     'id'), coalesce((SELECT MAX(id) FROM product_category),     0) + 1, false);
SELECT setval(pg_get_serial_sequence('product',              'id'), coalesce((SELECT MAX(id) FROM product),              0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_option',       'id'), coalesce((SELECT MAX(id) FROM product_option),       0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_option_value', 'id'), coalesce((SELECT MAX(id) FROM product_option_value), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('product_variant',      'id'), coalesce((SELECT MAX(id) FROM product_variant),      0) + 1, false);