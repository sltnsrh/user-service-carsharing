DELETE rt FROM `refresh_tokens` rt
        JOIN `users` u ON rt.user_id = u.id
        WHERE u.email = 'user@gmail.com';
DELETE ct FROM `confirmation_tokens` ct
        JOIN `users` u ON ct.user_id = u.id
        WHERE u.email = 'user@gmail.com';
DELETE b FROM `balances` b
        JOIN `users` u ON b.user_id = u.id
        WHERE u.email = 'user@gmail.com';
DELETE ur FROM `users_roles` ur
        JOIN `users` u ON ur.user_id = u.id
        WHERE u.email = 'user@gmail.com';
DELETE FROM `users` WHERE email = 'user@gmail.com';
