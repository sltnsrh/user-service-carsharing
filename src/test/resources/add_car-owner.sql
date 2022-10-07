UPDATE `users`
SET `driver_licence` = NULL
WHERE `driver_licence` = 'HFY123456';

INSERT INTO `users` (age, email, first_name, last_name, password, driver_licence, status_id)
VALUES (21, 'owner@gmail.com', 'Bob', 'Alister',
        '$2a$10$sivsP.3gyr3Yy8aHZ.02S.5yJ7znZBvzDQ0nGpb2r4e0MKOiiJQeC', 'HFY123456', 1);

INSERT INTO `users_roles` (user_id, role_id)
    SELECT `id`, 3 FROM `users` WHERE `email` = 'owner@gmail.com';

INSERT INTO `balances` (user_id, currency, value)
    SELECT `id`, 'UAH', 0.00 FROM `users` WHERE `email` = 'owner@gmail.com';
