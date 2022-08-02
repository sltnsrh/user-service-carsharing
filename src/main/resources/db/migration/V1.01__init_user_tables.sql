CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `roles` (id, role_name) VALUES (1, 'ADMIN'),
                                           (2, 'USER'),
                                           (3, 'CAR_OWNER');

CREATE TABLE IF NOT EXISTS `statuses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `status_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `statuses` (id, status_type) VALUES (1, 'ENABLE'),
                                                (2, 'DISABLE');

CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `age` int DEFAULT NULL,
  `driver_licence` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `status_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`email`),
  KEY (`status_id`),
  CONSTRAINT FOREIGN KEY (`status_id`) REFERENCES `statuses` (`id`)
);

CREATE TABLE IF NOT EXISTS `users_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY (`role_id`),
  CONSTRAINT FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
);
