-- Local development seed data. Existing records are never overwritten.
-- Supplied RSA keys and password hashes are development fixtures only.
-- Do not seed JWTs: the supplied token claims did not match the account rows.

INSERT INTO `role` (`id`, `name`)
SELECT 1, '管理員'
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `id` = 1 OR `name` = '管理員');

INSERT INTO `role` (`id`, `name`)
SELECT 2, '補習班'
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `id` = 2 OR `name` = '補習班');

INSERT INTO `role` (`id`, `name`)
SELECT 3, '補習班老師'
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `id` = 3 OR `name` = '補習班老師');

INSERT INTO `role` (`id`, `name`)
SELECT 4, '家長'
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `id` = 4 OR `name` = '家長');

INSERT INTO `account` (`id`, `account`, `password`, `name`, `role`, `phone`, `email`, `status`, `public_key`, `private_key`, `token`)
SELECT 1, 'admin', '$2a$10$D.A7jaYveLPKUiYsayVb6.1muysfrdqT5iKTpFBl6pxjnz6q5fDI.', '管理員', 1, '0912345678', 'test@test.com', 1,
'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',
NULL
WHERE NOT EXISTS (SELECT 1 FROM `account` WHERE `id` = 1 OR `account` = 'admin');

INSERT INTO `account` (`id`, `account`, `password`, `name`, `role`, `phone`, `email`, `status`, `public_key`, `private_key`, `token`)
SELECT 2, 'test', '$2a$10$/deCb62dHmckFjAvMIjiu.7fsY6wWqnbDVmvqakXsiU1IGITk7McW', '大中天', 4, '0912345678', 'test@test.com', 1,
'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',
NULL
WHERE NOT EXISTS (SELECT 1 FROM `account` WHERE `id` = 2 OR `account` = 'test');

INSERT INTO `payment_gateway` (`id`, `merchant_id`, `hash_key`, `hash_iv`, `return_url`, `notify_url`, `api_url`)
SELECT 1, '3002599', 'spPjZn66i0OhqJsQ', 'hT5OJckN45isQTTs',
       'https://test.com', 'https://test.com', 'https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5'
WHERE NOT EXISTS (SELECT 1 FROM `payment_gateway` WHERE `id` = 1 OR `merchant_id` = '3002599');
