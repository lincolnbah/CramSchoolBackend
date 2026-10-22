-- Development initialization only: existing tables and their data are preserved.
-- This script does not alter existing table definitions or insert seed data.

CREATE TABLE IF NOT EXISTS `account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account` varchar(50) NOT NULL COMMENT '帳號',
  `password` varchar(255) NOT NULL COMMENT '密碼',
  `name` varchar(50) NOT NULL COMMENT '名稱',
  `role` int NOT NULL DEFAULT '4' COMMENT '腳色預設家長(4)',
  `phone` varchar(50) DEFAULT NULL COMMENT '電話',
  `email` varchar(255) DEFAULT NULL COMMENT '信箱',
  `line_id` varchar(255) DEFAULT NULL COMMENT 'line 登錄用',
  `google_id` varchar(255) DEFAULT NULL COMMENT 'google 登錄用',
  `status` int NOT NULL DEFAULT '1' COMMENT '狀態, 1 啟用, 0 停用',
  `public_key` text NOT NULL COMMENT '公鑰',
  `private_key` text NOT NULL COMMENT '私鑰',
  `token` text COMMENT 'jwt token',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帳號';

CREATE TABLE IF NOT EXISTS `api` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'uid',
  `path` varchar(255) NOT NULL COMMENT 'api path',
  `method` int NOT NULL COMMENT 'api method',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='api配置';

CREATE TABLE IF NOT EXISTS `menu` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'uid',
  `name` varchar(255) NOT NULL COMMENT '功能名稱',
  `type` int NOT NULL COMMENT '功能型態, MENU / BUTTON',
  `router` varchar(255) NOT NULL DEFAULT '' COMMENT '前端router展示',
  `parent_id` int NOT NULL COMMENT '父層uid',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能菜單';

CREATE TABLE IF NOT EXISTS `menu_api` (
  `id` int NOT NULL AUTO_INCREMENT,
  `menu_id` int NOT NULL,
  `api_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='mapping表';

CREATE TABLE IF NOT EXISTS `role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '名稱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE IF NOT EXISTS `role_menu` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int NOT NULL,
  `menu_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='mapping表';

CREATE TABLE IF NOT EXISTS `payment_gateway` (
  `id` int NOT NULL AUTO_INCREMENT,
  `merchant_id` varchar(255) NOT NULL COMMENT '商戶號',
  `hash_key` text,
  `hash_iv` text,
  `return_url` varchar(255) NOT NULL COMMENT '支付完成用戶跳轉頁面',
  `api_url` varchar(255) NOT NULL COMMENT '金流三方api',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金流配置';

CREATE TABLE IF NOT EXISTS `order` (
`id` BIGINT NOT NULL AUTO_INCREMENT,
`account_id` INT NOT NULL COMMENT '用戶uid',
`order_no` VARCHAR(255) NOT NULL COMMENT '訂單號',
`third_order_no` VARCHAR(45) NULL COMMENT '金流方訂單號',
`amount` DECIMAL(12,2) NOT NULL COMMENT '金額',
`fee` DECIMAL(12,2) NULL COMMENT '金流方手續費',
`payment_type` VARCHAR(255) NULL COMMENT '用戶付款方式',
`payment_time` DATETIME NULL COMMENT '用戶付款時間',
`status` INT NOT NULL COMMENT '狀態, 1 待處理, 2 處理中, 3 失敗, 4 成功, 5 取消',
`payment_gateway` INT NULL COMMENT '三方金流',
`message` TEXT NULL COMMENT '三方交易訊息',
`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
PRIMARY KEY (`id`),
UNIQUE KEY `orderNo` (`order_no`)
) DEFAULT CHARSET=utf8mb4 COMMENT = '訂單';

