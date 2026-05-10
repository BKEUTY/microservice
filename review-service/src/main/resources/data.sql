DELETE FROM admin_replies;
DELETE FROM review_images;
DELETE FROM reviews;

INSERT INTO reviews (id, user_id, variant_id, rating, comment, is_hidden, is_replied, created_at, updated_at) VALUES
(1, '593547b7-eb5c-453e-84dd-73b70a331198', 1, 5, 'Sản phẩm giao nhanh, chất lượng tuyệt vời, mình rất thích!', false, false, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(2, '593547b7-eb5c-453e-84dd-73b70a331198', 2, 5, 'Chất lượng ổn, bao bì đẹp, hơi đắt một chút.', false, true, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
(3, '593547b7-eb5c-453e-84dd-73b70a331198', 1, 4, 'Dùng rất thích, da mịn màng hơn hẳn.', false, false, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(4, '593547b7-eb5c-453e-84dd-73b70a331198', 1, 5, 'Mua lần thứ 2 rồi vẫn rất ưng ý.', false, false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(5, '593547b7-eb5c-453e-84dd-73b70a331198', 1, 3, 'Bình thường, không có gì quá nổi bật.', false, false, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(6, '593547b7-eb5c-453e-84dd-73b70a331198', 1, 5, 'Sản phẩm chính hãng, check mã vạch ok.', false, false, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(7, '593547b7-eb5c-453e-84dd-73b70a331198', 1, 4, 'Khá tốt, sẽ ủng hộ shop tiếp.', false, false, NOW() - INTERVAL '36 hours', NOW() - INTERVAL '36 hours'),
(8, '593547b7-eb5c-453e-84dd-73b70a331198', 1, 4, 'Đáng đồng tiền bát gạo.', false, false, NOW() - INTERVAL '24 hours', NOW() - INTERVAL '24 hours'),
(9, '593547b7-eb5c-453e-84dd-73b70a331198', 2, 4, 'Giao hàng hơi lâu nhưng sản phẩm rất tốt.', false, false, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),
(10, '593547b7-eb5c-453e-84dd-73b70a331198', 3, 5, 'Tuyệt vời, sẽ quay lại!', false, false, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(11, '593547b7-eb5c-453e-84dd-73b70a331198', 4, 4, 'Dùng ổn, đáng mua.', false, false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
(12, '593547b7-eb5c-453e-84dd-73b70a331198', 5, 5, 'Rất hài lòng với sản phẩm này.', false, false, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

INSERT INTO review_images (review_id, image_url) VALUES 
(1, 'https://picsum.photos/400/400?random=1'),
(1, 'https://picsum.photos/400/400?random=2'),
(6, 'https://picsum.photos/400/400?random=3');

INSERT INTO admin_replies (id, review_id, comment, admin_id, replied_at, updated_at) VALUES 
(1, 2, 'Cảm ơn bạn đã quan tâm đến sản phẩm, chúng tôi sẽ xem xét để cải thiện mức giá phù hợp hơn trong tương lai.', 'admin-1', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

SELECT setval(pg_get_serial_sequence('reviews', 'id'), coalesce((SELECT MAX(id) FROM reviews), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('admin_replies', 'id'), coalesce((SELECT MAX(id) FROM admin_replies), 0) + 1, false);
