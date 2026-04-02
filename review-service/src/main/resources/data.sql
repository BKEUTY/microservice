DELETE FROM admin_replies;
DELETE FROM review_images;
DELETE FROM reviews;

INSERT INTO reviews (id, user_id, variant_id, rating, comment, is_hidden, created_at, updated_at) VALUES
(1, 'user-1', 1, 5, 'Sản phẩm giao nhanh, chất lượng tuyệt vời, mình rất thích!', false, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');
INSERT INTO reviews (id, user_id, variant_id, rating, comment, is_hidden, created_at, updated_at) VALUES
(2, 'user-2', 2, 4, 'Chất lượng ổn, bao bì đẹp, hơi đắt một chút.', false, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');
INSERT INTO reviews (id, user_id, variant_id, rating, comment, is_hidden, created_at, updated_at) VALUES
(3, 'user-3', 1, 1, 'Giao hàng chậm, hộp bị móp méo.', false, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');
INSERT INTO reviews (id, user_id, variant_id, rating, comment, is_hidden, created_at, updated_at) VALUES
(4, 'user-4', 3, 2, 'Sản phẩm có dấu hiệu đã qua sử dụng, yêu cầu hoàn tiền.', true, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');
INSERT INTO reviews (id, user_id, variant_id, rating, comment, is_hidden, created_at, updated_at) VALUES
(5, 'user-5', 2, 5, 'Tuyệt vời.', false, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');
INSERT INTO review_images (review_id, image_url) VALUES 
(1, 'https://picsum.photos/400/400?random=1'),
(1, 'https://picsum.photos/400/400?random=2');
INSERT INTO admin_replies (id, review_id, comment, admin_id, replied_at, updated_at) VALUES 
(1, 2, 'Cảm ơn bạn đã quan tâm đến sản phẩm, chúng tôi sẽ xem xét để cải thiện mức giá phù hợp hơn trong tương lai.', 'admin-1', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');
INSERT INTO admin_replies (id, review_id, comment, admin_id, replied_at, updated_at) VALUES 
(2, 3, 'Chào bạn, rất xin lỗi vì trải nghiệm không tốt của bạn. Bộ phận CSKH sẽ liên hệ hỗ trợ bạn đổi trả hàng.', 'admin-1', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

SELECT setval(pg_get_serial_sequence('reviews', 'id'), coalesce((SELECT MAX(id) FROM reviews), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('admin_replies', 'id'), coalesce((SELECT MAX(id) FROM admin_replies), 0) + 1, false);
