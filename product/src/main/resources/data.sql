-- DATA DỰ PHÒNG CHO PRODUCT SERVICE
-- XÓA SẠCH DATA TRƯỚC KHI THÊM MỚI
DELETE FROM variant_option_values;
DELETE FROM product_variant;
DELETE FROM product_option_value;
DELETE FROM product_option;
DELETE FROM product_categories;
DELETE FROM product;
DELETE FROM product_category;

-- 1. DANH MỤC SẢN PHẨM (product_category)
INSERT INTO product_category (id, category_name) VALUES
(1, 'Chăm sóc da'),
(2, 'Trang điểm'),
(3, 'Nước hoa'),
(4, 'Chăm sóc tóc'),
(5, 'Dụng cụ làm đẹp'),
(6, 'Bộ Quà Tặng'),
(7, 'Sữa rửa mặt'),
(8, 'Nước cân bằng'),
(9, 'Tinh chất'),
(10, 'Kem dưỡng'),
(11, 'Chống nắng'),
(12, 'Trang điểm mặt'),
(13, 'Trang điểm môi'),
(14, 'Trang điểm mắt')
ON CONFLICT (id) DO NOTHING;

-- DATA DỰ PHÒNG CHO BRAND
DELETE FROM brand;

-- Cấu trúc giả định: id, name, description, image, brand_status
INSERT INTO brand (id, name, description, image, brand_status) VALUES
(1, 'CeraVe', 'Thương hiệu dược mỹ phẩm CeraVe', 'cerave_logo.png', 'ACTIVE'),
(2, 'Cetaphil', 'Chăm sóc da dịu nhẹ', 'cetaphil_logo.png', 'ACTIVE'),
(3, 'La Roche-Posay', 'Dược mỹ phẩm Pháp', 'lrp_logo.png', 'ACTIVE'),
(4, '3CE', 'Thương hiệu mỹ phẩm Hàn Quốc', '3ce_logo.png', 'ACTIVE'),
(5, 'Dior', 'Thương hiệu cao cấp', 'dior_logo.png', 'ACTIVE'),
(6, 'Estee Lauder', 'Chăm sóc da cao cấp', 'estee_logo.png', 'ACTIVE'),
(7, 'Khác', 'Các thương hiệu khác', 'default_brand.png', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- TRONG PHẦN SẢN PHẨM (product), BẠN CẦN THÊM CỘT brand_id
-- Xóa bảng product cũ và insert lại với brand_id tương ứng
DELETE FROM product;

INSERT INTO product (id, name, description, image, status, brand_id) VALUES
(1, 'Sữa Rửa Mặt CeraVe Foaming Cleanser', 'Sữa rửa mặt tạo bọt làm sạch sâu cho da dầu.', 'cerave_foaming.jpg', 0, 1),
(2, 'Sữa Rửa Mặt Cetaphil Gentle Skin Cleanser', 'Công thức dịu nhẹ không gây kích ứng.', 'cetaphil_gentle.jpg', 0, 2),
(3, 'Sữa Rửa Mặt La Roche-Posay Effaclar', 'Gel rửa mặt cho da dầu nhạy cảm.', 'lrp_effaclar.jpg', 0, 3),
(4, 'Sữa Rửa Mặt Cosrx Low pH Good Morning', 'Cân bằng độ pH tự nhiên cho da.', 'cosrx_low_ph.jpg', 0, 7),
(5, 'Sữa Rửa Mặt Innisfree Green Tea', 'Chiết xuất trà xanh giúp sạch sâu.', 'innisfree_greentea.jpg', 0, 7),
(6, 'Kem Chống Nắng Anessa Perfect UV', 'Công nghệ Aqua Booster chống nước mạnh mẽ.', 'anessa_uv.jpg', 0, 7),
(7, 'Kem Chống Nắng La Roche-Posay Anthelios', 'Khả năng chống nắng quang phổ rộng.', 'lrp_anthelios.jpg', 0, 3),
(8, 'Kem Chống Nắng Skin1004 Madagascar', 'Chiết xuất rau má làm dịu da.', 'skin1004_sun.jpg', 0, 7),
(9, 'Kem Chống Nắng Cell Fusion C Laser', 'Dành cho da sau điều trị laser.', 'cell_fusion_laser.jpg', 0, 7),
(10, 'Kem Chống Nắng L''Oreal Invisible Fluid', 'Kết cấu mỏng nhẹ không để lại vệt trắng.', 'loreal_invisible.jpg', 0, 7),
(11, 'Nước Hoa Hồng Lancôme Tonique Confort', 'Dưỡng ẩm sâu cho da khô.', 'lancome_toner.jpg', 0, 7),
(12, 'Toner Kiehl''s Calendula Herbal', 'Chiết xuất hoa cúc làm dịu da.', 'kiehls_calendula.jpg', 0, 7),
(13, 'Toner Mamonde Rose Water', '90.97% tinh chất hoa hồng.', 'mamonde_rose.jpg', 0, 7),
(14, 'Nước Hoa Hồng Klairs Supple Preparation', 'Cấp ẩm tức thì cho da.', 'klairs_toner.jpg', 0, 7),
(15, 'Toner Paula''s Choice Skin Balancing', 'Se khít lỗ chân lông.', 'paula_balancing.jpg', 0, 7),
(16, 'Serum Estee Lauder Advanced Night Repair', 'Phục hồi da ban đêm thần thánh.', 'estee_anr.jpg', 0, 6),
(17, 'Serum The Ordinary Niacinamide 10% + Zinc 1', 'Giảm thâm mụn và thu nhỏ lỗ chân lông.', 'to_niacinamide.jpg', 0, 7),
(18, 'Serum Skin1004 Madagascar Centella', '100% chiết xuất rau má tinh khiết.', 'skin1004_serum.jpg', 0, 7),
(19, 'Serum Klairs Freshly Juiced Vitamin C', 'Làm sáng da và mờ thâm.', 'klairs_vitc.jpg', 0, 7),
(20, 'Serum Timeless Vitamin B5 + Hyaluronic Acid', 'Cấp ẩm và phục hồi da tổn thương.', 'timeless_b5.jpg', 0, 7),
(21, 'Kem Dưỡng Neutrogena Hydro Boost Water Gel', 'Cấp ẩm chuyên sâu dạng gel.', 'neutrogena_gel.jpg', 0, 7),
(22, 'Kem Dưỡng Kiehl''s Ultra Facial Cream', 'Dưỡng ẩm suốt 24 giờ.', 'kiehls_ultra.jpg', 0, 7),
(23, 'Kem Dưỡng Phục Hồi La Roche-Posay Cicaplast B5', 'Hỗ trợ tái tạo màng bảo vệ da.', 'lrp_b5.jpg', 0, 3),
(24, 'Kem Dưỡng Clinique Dramatically Different', 'Cung cấp độ ẩm tối ưu.', 'clinique_yellow.jpg', 0, 7),
(25, 'Kem Dưỡng Laneige Water Bank Blue HA', 'Công nghệ HA mới dưỡng ẩm sâu.', 'laneige_waterbank.jpg', 0, 7),
(26, 'Phấn Nước Laneige Neo Cushion Matte', 'Che phủ cao và lâu trôi.', 'laneige_cushion.jpg', 0, 7),
(27, 'Kem Nền Estee Lauder Double Wear', 'Lớp nền hoàn hảo trứ danh.', 'estee_doublewear.jpg', 0, 6),
(28, 'Phấn Phủ Mac Studio Fix Powder Plus', 'Kết hợp nền và phấn 2 trong 1.', 'mac_studiofix.jpg', 0, 7),
(29, 'Cushion Hera Black Cushion', 'Đẳng cấp trang điểm Hàn Quốc.', 'hera_black.jpg', 0, 7),
(30, 'Kem Nền Maybelline Fit Me', 'Kiềm dầu hiệu quả.', 'fitme_foundation.jpg', 0, 7),
(31, 'Son Thỏi 3CE Velvet Lip Tint', 'Màu sắc trẻ trung, chất son mịn.', '3ce_lip.jpg', 0, 4),
(32, 'Son Kem Romand Juicy Lasting Tint', 'Hiệu ứng căng bóng môi.', 'romand_juicy.jpg', 0, 7),
(33, 'Son Dưỡng Dior Addict Lip Glow', 'Son dưỡng huyền thoại biến hóa màu sắc.', 'dior_lipglow.jpg', 0, 5),
(34, 'Son Thỏi MAC Matte Lipstick', 'Màu son chuẩn, lâu trôi.', 'mac_matte.jpg', 0, 7),
(35, 'Son Kem Black Rouge Air Fit Velvet Tint', 'Mềm lòng với bảng màu đa dạng.', 'blackrouge_tint.jpg', 0, 7),
(36, 'Nước Hoa Dior Sauvage Eau De Parfum', 'Mạnh mẽ, nam tính.', 'dior_sauvage.jpg', 0, 5),
(37, 'Nước Hoa Chanel Coco Mademoiselle', 'Quyến rũ và sang trọng.', 'chanel_coco.jpg', 0, 7),
(38, 'Bộ Quà Tặng Skincare L''Oreal Paris', 'Sự kết hợp hoàn hảo cho làn da.', 'loreal_giftset.jpg', 0, 7),
(39, 'Dụng Cụ Massage Nâng Cơ ReFa Carat', 'Công nghệ microcurrent tiên tiến.', 'refa_carat.jpg', 0, 7),
(40, 'Máy Rửa Mặt Foreo Luna 4', 'Làm sạch sâu gấp 35 lần.', 'foreo_luna4.jpg', 0, 7)
ON CONFLICT (id) DO NOTHING;

-- 3. LIÊN KÊT SẢN PHẨM - DANH MỤC (product_categories)
INSERT INTO product_categories (product_id, category_id) VALUES
(1, 1), (1, 7), (2, 1), (2, 7), (3, 1), (3, 7), (4, 1), (4, 7), (5, 1), (5, 7),
(6, 1), (6, 11), (7, 1), (7, 11), (8, 1), (8, 11), (9, 1), (9, 11), (10, 1), (10, 11),
(11, 1), (11, 8), (12, 1), (12, 8), (13, 1), (13, 8), (14, 1), (14, 8), (15, 1), (15, 8),
(16, 1), (16, 9), (17, 1), (17, 9), (18, 1), (18, 9), (19, 1), (19, 9), (20, 1), (20, 9),
(21, 1), (21, 10), (22, 1), (22, 10), (23, 1), (23, 10), (24, 1), (24, 10), (25, 1), (25, 10),
(26, 2), (26, 12), (27, 2), (27, 12), (28, 2), (28, 12), (29, 2), (29, 12), (30, 2), (30, 12),
(31, 2), (31, 13), (32, 2), (32, 13), (33, 2), (33, 13), (34, 2), (34, 13), (35, 2), (35, 13),
(36, 3), (37, 3), (38, 6), (39, 5), (40, 5)
ON CONFLICT DO NOTHING;

-- 4. TÙY CHỌN SẢN PHẨM (product_option)
INSERT INTO product_option (id, option_name, product_id, status) VALUES
(1, 'Dung tích', 1, 0),
(2, 'Dung tích', 2, 0),
(3, 'Dung tích', 3, 0),
(4, 'Dung tích', 6, 0),
(5, 'Màu sắc', 31, 0),
(6, 'Màu sắc', 32, 0),
(7, 'Màu sắc', 33, 0),
(8, 'Loại da', 21, 0),
(9, 'Dung tích', 16, 0),
(10, 'Màu sắc', 26, 0)
ON CONFLICT (id) DO NOTHING;

-- 5. GIÁ TRỊ TÙY CHỌN (product_option_value)
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
(19, 'Tone 21N', 10, 0), (20, 'Tone 23N', 10, 0)
ON CONFLICT (id) DO NOTHING;

-- 6. BIẾN THỂ SẢN PHẨM (product_variant)
INSERT INTO product_variant (id, product_id, product_variant_name, price, stock_quantity, description, status, product_image_url) VALUES
(1, 1, 'CeraVe Foaming 236ml', 350000, 100, 'Chai nhỏ tiện lợi', 0, 'cerave_236.jpg'),
(2, 1, 'CeraVe Foaming 473ml', 550000, 50, 'Tiết kiệm hơn', 0, 'cerave_473.jpg'),
(3, 2, 'Cetaphil 125ml', 150000, 200, NULL, 0, 'cetaphil_125.jpg'),
(4, 2, 'Cetaphil 500ml', 380000, 150, NULL, 0, 'cetaphil_500.jpg'),
(5, 31, 'Son 3CE - 126', 380000, 80, 'Màu đỏ gạch', 0, '3ce_126.jpg'),
(6, 31, 'Son 3CE - 114', 380000, 60, 'Màu cam đào', 0, '3ce_114.jpg'),
(7, 33, 'Dior Lipglow Pink', 850000, 40, 'Hồng tự nhiên', 0, 'dior_pink.jpg'),
(8, 33, 'Dior Lipglow Coral', 850000, 30, 'Cam san hô', 0, 'dior_coral.jpg'),
(9, 16, 'Estee Lauder 30ml', 2100000, 20, NULL, 0, 'estee_30.jpg'),
(10, 16, 'Estee Lauder 50ml', 3400000, 15, NULL, 0, 'estee_50.jpg'),
(11, 21, 'Neutrogena Gel - Da dầu', 450000, 120, 'Cực kỳ mỏng nhẹ', 0, 'neutrogena_oil.jpg'),
(12, 6, 'Anessa 20ml', 250000, 300, 'Bản mini', 0, 'anessa_20.jpg'),
(13, 3, 'LRP Effaclar Default', 420000, 100, NULL, 0, 'lrp_effaclar.jpg'),
(14, 4, 'Cosrx Default', 280000, 150, NULL, 0, 'cosrx.jpg'),
(15, 7, 'LRP Anthelios Default', 490000, 80, NULL, 0, 'lrp_anthelios.jpg'),
(16, 8, 'Skin1004 Sun Default', 320000, 90, NULL, 0, 'skin1004.jpg'),
(17, 36, 'Dior Sauvage 100ml', 3200000, 25, NULL, 0, 'sauvage.jpg'),
(18, 40, 'Foreo Luna 4 Default', 5500000, 10, NULL, 0, 'luna4.jpg'),
(19, 12, 'Kiehls Calendula 250ml', 1100000, 40, NULL, 0, 'calendula.jpg'),
(20, 22, 'Kiehls Ultra Facial 50ml', 950000, 50, NULL, 0, 'ultrafacial.jpg')
ON CONFLICT (id) DO NOTHING;

-- 7. LIÊN KẾẾT BIẾẾN THÊẾ - TÙY CHỌN (variant_option_values)
INSERT INTO variant_option_values (variant_id, option_value_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 9), (6, 10), (7, 13), (8, 14), (9, 17), (10, 18), (11, 15)
ON CONFLICT DO NOTHING;
