TRUNCATE TABLE promotion_membership_level, promotion_birthday_month, promotion_brand_ids, promotion_product_ids, promotion_category_ids, promotion RESTART IDENTITY CASCADE;

INSERT INTO promotion (id, title, description, start_at, end_at, status, discount_type, discount_value, max_discount, promotion_type, code, total_quantity, remaining_quantity, min_order_value, usage_limit_per_user) VALUES
(10, 'Siêu Voucher Tháng 5',
    'Chào hè rực rỡ, giảm 20% tối đa 100k cho đơn từ 500k',
    '2026-05-01 00:00:00', '2026-05-31 23:59:59',
    0, 0, 20, 100000, 'VoucherPromotion', 'HELLOHE', 500, 500, 500000.0, 1),

(9, 'Mã Giảm Giá Tân Binh',
    'Dành cho khách hàng mới, giảm 50k cho đơn hàng từ 200k',
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    0, 1, 50000, 50000, 'VoucherPromotion', 'BKEUTYNEW', 1000, 999, 200000.0, 1),

(7, 'Ưu đãi mùa hè',
    'Giảm 25% tất cả sản phẩm vào mùa hè',
    '2026-05-01 00:00:00', '2026-05-31 23:59:59',
    0, 0, 25, 100000, 'ProductPromotion', NULL, NULL, NULL, NULL, NULL),

(13, 'Voucher Cháy Hàng',
    'Mã giảm giá giới hạn chỉ 100 lượt dùng',
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    0, 1, 20000, 20000, 'VoucherPromotion', 'LOWSTOCK', 100, 5, 100000.0, 1),

(6, 'Tri Ân Hạng Bạc',
    'Giảm thêm 5%, tối đa 20k cho khách hàng hạng Bạc',
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    0, 0, 5, 20000, 'UserPromotion', NULL, NULL, NULL, NULL, NULL),

(1, 'Flash Sale Tháng 3',
    'Giảm 20% toàn bộ danh mục Chăm sóc da và Trang điểm, tối đa 80k',
    '2026-03-01 00:00:00', '2026-03-31 23:59:59',
    1, 0, 20, 80000, 'ProductPromotion', NULL, NULL, NULL, NULL, NULL),

(8, 'Mùa tựu trường',
    'Khuyến mãi vào mùa tựu trường năm nay, giảm 15% cho toàn bộ sản phẩm',
    '2026-09-01 00:00:00', '2026-10-31 23:59:59',
    3, 0, 15, 100000, 'ProductPromotion', NULL, NULL, NULL, NULL, NULL),

(4, 'Giảm Riêng Serum',
    'Giảm 15% dành cho các sản phẩm Serum',
    '2026-03-01 00:00:00', '2026-03-31 23:59:59',
    1, 0, 15, 300000, 'ProductPromotion', NULL, NULL, NULL, NULL, NULL),

(12, 'Khuyến Mãi Khủng Cuối Năm',
    'Lễ hội mua sắm cuối năm, giảm 50% tối đa 300k cho đơn từ 2 triệu',
    '2026-11-01 00:00:00', '2026-12-31 23:59:59',
    3, 0, 50, 300000, 'VoucherPromotion', 'YEAREND', 100, 100, 2000000.0, 1),

(14, 'Voucher Tiết Kiệm',
    'Giảm ngay 10% không giới hạn giá trị giảm tối đa',
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    2, 0, 10, 9999999, 'VoucherPromotion', 'SAVE10', 1000, 1000, 0.0, 1),

(11, 'Voucher VIP Member',
    'Tri ân khách hàng thân thiết, giảm 15% tối đa 150k cho đơn từ 1 triệu',
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    0, 0, 15, 150000, 'VoucherPromotion', 'VIPONLY', 2000, 1999, 1000000.0, 2),

(3, 'Ưu Đãi Thương Hiệu Estee Lauder',
    'Giảm 10% cho Estee Lauder, tối đa 200k',
    '2026-03-15 00:00:00', '2026-05-15 23:59:59',
    1, 0, 10, 200000, 'ProductPromotion', NULL, NULL, NULL, NULL, NULL),

(2, 'Ưu Đãi Thương Hiệu CeraVe',
    'Giảm 50k cố định cho toàn bộ sản phẩm CeraVe',
    '2026-03-01 00:00:00', '2026-04-30 23:59:59',
    1, 1, 50000, 50000, 'ProductPromotion', NULL, NULL, NULL, NULL, NULL),

(5, 'Quà Tặng Sinh Nhật',
    'Giảm 40k cho khách sinh nhật tháng 3 và tháng 10',
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    0, 1, 15000, 15000, 'UserPromotion', NULL, NULL, NULL, NULL, NULL),

(16, 'Giam3 giá VIP KIM CƯƠNG',
    'vip kim cương giảm 40%',
    '2026-05-01 00:00:00', '2026-06-07 00:00:00',
    0, 0, 40, 300000, 'VoucherPromotion', 'VIPKC', 200, 200, 500000.0, 2),

(15, 'VIP KIM CƯƠNG',
    'rất tốt cho khách hàng kim cuong',
    '2026-05-08 00:00:00', '2026-05-18 00:00:00',
    1, 0, 40, 200000, 'UserPromotion', NULL, NULL, NULL, NULL, NULL);

INSERT INTO promotion_category_ids (promotion_id, category_id) VALUES
(1, 1),
(1, 2),
(4, 7);

INSERT INTO promotion_brand_ids (promotion_id, brand_id) VALUES
(2, 1),
(3, 3);

INSERT INTO promotion_product_ids (promotion_id, product_id) VALUES
(4, 16);

INSERT INTO promotion_birthday_month (promotion_id, birthday_month) VALUES
(5, 3),
(5, 10);

INSERT INTO promotion_membership_level (promotion_id, membership_level) VALUES
(6, 1),
(11, 2),
(11, 3),
(11, 4),
(15, 4);

SELECT setval(pg_get_serial_sequence('promotion', 'id'), coalesce((SELECT MAX(id) FROM promotion), 0) + 1, false);
