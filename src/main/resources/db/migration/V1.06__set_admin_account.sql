INSERT INTO `users` (id, age, email, first_name, password, status_id)
VALUES (1, 0, 'admin@carsharing.com', 'Admin',
        '$2a$10$dyVD5NpQzVNO.px7s1aC3.yT8fCGzQOX7UFTQZe62d2gUUDS/Gtdu', 1);

INSERT INTO `users_roles` (user_id, role_id) VALUES (1, 1);

INSERT INTO `balances` (id, user_id, currency, value) VALUES (1, 1, 'UAH', 0.00);
