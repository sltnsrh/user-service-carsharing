INSERT INTO `users` (age, email, first_name, last_name, password, driver_licence, status_id)
VALUES (21, 'user@gmail.com', 'Bob', 'Alister',
        '$2a$10$sivsP.3gyr3Yy8aHZ.02S.5yJ7znZBvzDQ0nGpb2r4e0MKOiiJQeC', 'HFY123UYY', 1);

INSERT INTO `users_roles` (user_id, role_id)
    SELECT `id`, 2 FROM `users` WHERE `email` = 'user@gmail.com';

INSERT INTO `balances` (user_id, currency, value)
    SELECT `id`, 'UAH', 0.00 FROM `users` WHERE `email` = 'user@gmail.com';
