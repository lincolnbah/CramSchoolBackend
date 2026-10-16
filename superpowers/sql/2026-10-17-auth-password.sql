-- 先備份並確認目標為 cram_school，再手動執行；應用程式不會自動套用。
-- BCrypt 雜湊長度 60，原本 varchar(50) 無法儲存。
ALTER TABLE account MODIFY COLUMN password VARCHAR(255) NOT NULL COMMENT 'BCrypt 密碼雜湊';
-- 新增帳號時使用 PasswordEncoder.encode(...) 的結果，禁止儲存明文。
