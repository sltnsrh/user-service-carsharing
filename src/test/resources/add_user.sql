INSERT INTO `users` (id, age, email, first_name, last_name, password, driver_licence, status_id)
VALUES (2, 21, 'user@gmail.com', 'Bob', 'Alister',
        '$2a$10$sivsP.3gyr3Yy8aHZ.02S.5yJ7znZBvzDQ0nGpb2r4e0MKOiiJQeC', 'HFY123UYY', 1);

INSERT INTO `users_roles` (user_id, role_id) VALUES (2, 2);

INSERT INTO `balances` (id, user_id, currency, value) VALUES (2, 2, 'UAH', 0.00);
