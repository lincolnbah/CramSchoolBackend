-- 沿用既有 public_key、private_key、token 欄位，不新增或覆寫公私鑰。
-- 先確認目標為 cram_school 並備份。先查出重複帳號，若有結果先處理，不要直接執行 ALTER。
SELECT account, COUNT(*) AS duplicate_count FROM account GROUP BY account HAVING COUNT(*) > 1;
-- 若尚無 account 欄位的唯一索引，部署公開註冊前手動建立，以防止並行註冊重複帳號。
ALTER TABLE account ADD UNIQUE KEY uk_account_account (account);
