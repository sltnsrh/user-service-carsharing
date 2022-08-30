DROP TABLE IF EXISTS `refresh_tokens`;

CREATE TABLE `refresh_tokens` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `user_id` bigint NOT NULL,
                                  `expired_at` datetime(6) NOT NULL,
                                  `token` varchar(255) NOT NULL UNIQUE ,
                                  PRIMARY KEY (`id`),
                                  KEY (`user_id`),
                                  CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);