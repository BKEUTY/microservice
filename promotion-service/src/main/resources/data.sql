DELETE FROM promotion_membership_level;
DELETE FROM promotion_birthday_month;
DELETE FROM promotion_brand_ids;
DELETE FROM promotion_product_ids;
DELETE FROM promotion_category_ids;
DELETE FROM promotion;
 
INSERT INTO promotion (id, title, description, create_at, update_at, start_at, end_at, status, discount_type, discount_value, max_discount, promotion_type) VALUES
 
(1, 'Flash Sale Tháng 3',
    'Giảm 20% toàn bộ danh mục Chăm sóc da và Trang điểm, tối đa 80k',
    NOW(), NOW(),
    '2026-03-01 00:00:00', '2026-03-31 23:59:59',
    0, 0, 20, 80000, 'ProductPromotion'),
 
(2, 'Ưu Đãi Thương Hiệu Cetaphil',
    'Giảm 50k cố định cho toàn bộ sản phẩm Cetaphil',
    NOW(), NOW(),
    '2026-03-01 00:00:00', '2026-04-30 23:59:59',
    0, 1, 50000, 50000, 'ProductPromotion'),
 
(3, 'Ưu Đãi Thương Hiệu Estee Lauder',
    'Giảm 10% cho Estee Lauder, tối đa 200k',
    NOW(), NOW(),
    '2026-03-15 00:00:00', '2026-05-15 23:59:59',
    0, 0, 10, 200000, 'ProductPromotion'),
 
(4, 'Giảm Riêng Serum ANR',
    'Giảm 15% đúng sản phẩm Serum Estee Lauder Advanced Night Repair (product_id=16)',
    NOW(), NOW(),
    '2026-03-01 00:00:00', '2026-03-31 23:59:59',
    0, 0, 15, 300000, 'ProductPromotion'),
 
(5, 'Quà Tặng Sinh Nhật',
    'Giảm 50k cho khách sinh nhật tháng 3 và tháng 10',
    NOW(), NOW(),
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    0, 1, 50000, 50000, 'UserPromotion'),
 
(6, 'Tri Ân Hạng Đồng',
    'Giảm thêm 5%, tối đa 20k cho khách hàng hạng Đồng',
    NOW(), NOW(),
    '2026-01-01 00:00:00', '2026-12-31 23:59:59',
    0, 0, 5, 20000, 'UserPromotion'),
 
(7, 'Promotion Sắp Tới',
    'Chưa bắt đầu - dùng để test trạng thái INCOMING trên UI',
    NOW(), NOW(),
    '2026-05-01 00:00:00', '2026-05-31 23:59:59',
    0, 0, 25, 100000, 'ProductPromotion'),
 
(8, 'Promotion Đã Kết Thúc',
    'Đã hết hạn - dùng để test trạng thái EXPIRED trên UI',
    NOW(), NOW(),
    '2025-01-01 00:00:00', '2025-12-31 23:59:59',
    1, 0, 15, 100000, 'ProductPromotion');
 
INSERT INTO promotion_category_ids (promotion_id, category_id) VALUES
(1, 1),
(1, 2);
 
INSERT INTO promotion_brand_ids (promotion_id, brand_id) VALUES
(2, 2),
(3, 6);
 
INSERT INTO promotion_product_ids (promotion_id, product_id) VALUES
(4, 16);
 
INSERT INTO promotion_birthday_month (promotion_id, birthday_month) VALUES
(5, 3),
(5, 10);
 
INSERT INTO promotion_membership_level (promotion_id, membership_level) VALUES
(6, 1);
 
SELECT setval(pg_get_serial_sequence('promotion', 'id'), coalesce((SELECT MAX(id) FROM promotion), 0) + 1, false);