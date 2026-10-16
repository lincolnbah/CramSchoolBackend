DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS role_menu;
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS menu_api;
DROP TABLE IF EXISTS api;

CREATE TABLE account (id INT AUTO_INCREMENT PRIMARY KEY, account VARCHAR(50), password VARCHAR(255), role INT,
 name VARCHAR(50), phone VARCHAR(50), email VARCHAR(255), line_id VARCHAR(255), google_id VARCHAR(255),
 public_key TEXT, private_key TEXT, token TEXT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE role (id INT PRIMARY KEY, name VARCHAR(50));
CREATE TABLE role_menu (id INT PRIMARY KEY, role_id INT, menu_id INT);
CREATE TABLE menu (id INT PRIMARY KEY);
CREATE TABLE menu_api (id INT PRIMARY KEY, menu_id INT, api_id INT);
CREATE TABLE api (id INT PRIMARY KEY, path VARCHAR(255), method INT);

INSERT INTO role VALUES (1, '角色一'), (2, '角色二'), (3, '無綁定角色'), (4, '家長');
INSERT INTO account (id, account, password, role) VALUES (1, 'tester', 'fixture', 1), (2, 'orphan', 'fixture', 999),
 (3, 'duplicate', 'fixture', 1), (4, 'duplicate', 'fixture', 1), (5, 'duplicate', 'fixture', 1);
INSERT INTO menu VALUES (1), (2), (3);
INSERT INTO role_menu VALUES (1, 1, 1), (2, 1, 2), (3, 1, 999), (4, 2, 3);
INSERT INTO api VALUES (1, '/same', 1), (2, '/same', 2), (3, '/orphan-menu', 1), (4, '/unbound', 1);
INSERT INTO menu_api VALUES (1, 1, 1), (2, 2, 1), (3, 1, 2), (4, 999, 3), (5, 1, 999);
