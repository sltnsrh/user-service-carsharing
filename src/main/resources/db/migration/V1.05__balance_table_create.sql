DROP TABLE IF EXISTS `balances`;

CREATE TABLE `balances` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `user_id` bigint NOT NULL,
                            `currency` varchar(255) NOT NULL,
                            `value` decimal(19,2) NOT NULL,
                            PRIMARY KEY (`id`),
                            KEY (`user_id`),
                            CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);
