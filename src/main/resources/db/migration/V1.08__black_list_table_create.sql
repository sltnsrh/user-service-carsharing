DROP TABLE IF EXISTS `black_lists`;

CREATE TABLE `black_lists` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `user_id` bigint NOT NULL,
                            `jwt_token` varchar(255) NOT NULL,
                            `expiration_date` datetime(6) NOT NULL,
                            PRIMARY KEY (`id`),
                            KEY (`user_id`),
                            CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);
