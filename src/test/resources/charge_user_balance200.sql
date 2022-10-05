UPDATE `balances`
SET value = 200.00
WHERE user_id IN (SELECT `id` FROM `users` WHERE `email` = 'user@gmail.com');
