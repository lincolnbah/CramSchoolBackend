# 帳號專屬 RSA 登入

**Goal:** 註冊建立 RSA 公私鑰並保存於既有 account 欄位，前端查公鑰後加密密碼，登入解密後驗證 BCrypt。
**Why planning is required:** 變更登入契約、新增密鑰儲存及公開註冊流程。
**Acceptance:** 公鑰 API 不洩漏私鑰；各帳號有不同 RSA 金鑰；RSA-OAEP 的 SHA-256 與 MGF1 SHA-256 相容 Web Crypto；私鑰在 DB 中經 AES-GCM 加密；明文/錯誤帳號金鑰/修改密文均不能登入；JWT 與既有權限保持有效；JDK 8 測試通過。

### Outcome 1: DB 契約及金鑰生命週期
- Work: 沿用使用者新增的 public_key、private_key、token TEXT NOT NULL 欄位。產生 RSA 2048-bit 金鑰；公鑰為 SPKI Base64，私鑰為 PKCS8，先以獨立環境主金鑰 AES-GCM 加密後保存。未收到 token 用途偏好，依已告知預設保留欄位，不變更 JWT 驗證行為。提供帳號唯一索引 SQL，不自動執行資料庫變更。
- Verify: 隔離 H2 註冊、解密與資料持久化測試；實際 MySQL 僅唯讀查詢。

### Outcome 2: 公鑰、註冊及登入 API
- Work: POST /auth/public-key 接收 account，僅回 publicKey、格式與演算法。POST /auth/login 的 password 改為 Base64 RSA 密文。POST /auth/register 經 HTTPS 接收初始密碼，由伺服器指定家長角色 4；不接受前端指定角色。註冊成功產生金鑰與 BCrypt 雜湊。缺少密鑰/錯誤密文統一登入錯誤。查公鑰目前會反映帳號存在性，文件明示；HTTPS 必須保留。
- Verify: 完整 Security FilterChain 驗證端點開放範圍、traceId、無明文登入降級、JWT 拒絕與權限不變。

### Outcome 3: 前端串接與驗證
- Work: 提供 Web Crypto importKey/encrypt/login 範例及主金鑰設定；註冊需要先部署唯一索引；舊帳號缺少金鑰不自動產生、不覆蓋資料。
- Verify: JDK 8 Maven 測試，涵蓋跨帳號密鑰、損壞/短密文、錯誤 OAEP 參數、加密私鑰被修改、重複註冊、角色不可提升與每次 RSA 密文隨機化。

### 驗證結果
- JDK 8 `AUTH_DB_TEST=true mvn -o test`：26 tests、0 failures、0 errors、0 skipped。
- Java 8 / Web Crypto 互通：使用實際前端範例與 Node Web Crypto 加密，交給 Java 8 AccountKeyService 解密成功；HTTP 帳密呼叫被範例拒絕。
- MySQL 僅執行唯讀測試，未寫入帳號、角色、公私鑰，未執行唯一索引 SQL。
- 部署尚須設定 ACCOUNT_KEY_SECRET、HTTPS、角色 4 及帳號唯一索引；未知帳號公鑰查詢仍可反映存在性，RSA 密文目前無單次挑戰防重放。
