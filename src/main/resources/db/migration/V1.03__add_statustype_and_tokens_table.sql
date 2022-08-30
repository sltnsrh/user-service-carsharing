INSERT INTO `statuses` (id, status_type) VALUES (3, 'INVALIDATE');

DROP TABLE IF EXISTS `confirmation_tokens`;

CREATE TABLE `confirmation_tokens` (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `user_id` bigint NOT NULL,
                                       `confirmed_at` datetime(6) DEFAULT NULL,
                                       `created_at` datetime(6) NOT NULL,
                                       `expired_at` datetime(6) NOT NULL,
                                       `token` varchar(255) NOT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY (`user_id`),
                                       CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);
