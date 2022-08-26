DROP TABLE IF EXISTS `refresh_tokens`;

CREATE TABLE `refresh_tokens` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `expired_at` datetime(6) NOT NULL,
                                  `token` varchar(255) NOT NULL UNIQUE ,
                                  `user_id` bigint NOT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY (`user_id`),
                                  CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);