# 登入及 API 權限

本功能使用 JDK 8、Spring Boot 2.7、Spring Security、帳號專屬 RSA-OAEP SHA-256 與 HS256 簽章 JWT。

## 啟動前設定

1. 目前 DB 已有 `password VARCHAR(255)` 與 `public_key`、`private_key`、`token` 三個 TEXT NOT NULL 欄位，無須再次新增。公開註冊部署前確認帳號唯一索引，SQL 見 `superpowers/sql/2026-10-17-account-unique.sql`；腳本不會自動執行，先檢查重複帳號。
2. 先建立角色 `4`（家長），再透過註冊 API 建立帳號。密碼保存為 BCrypt 雜湊，不支援明文或 MD5。註冊需 8 至 72 字元，且 UTF-8 編碼最多 72 bytes。前端不能指定角色。
3. 產生獨立的 32 bytes 以上隨機金鑰，Base64 編碼後設定環境變數 `JWT_SECRET`。金鑰缺少、過短或格式不正確會拒絕啟動。不要使用測試程式中的固定金鑰。
4. 設定 HTTPS：可在 Spring Boot 設定 `server.ssl.key-store`、`server.ssl.key-store-password`、`server.ssl.key-store-type=PKCS12` 與連接埠。或在受信任的反向代理終止 TLS，再設定正確的轉送標頭處理；後端只允許受信任代理連入，避免客戶端偽造 HTTPS 狀態。
5. 另產生獨立的 32 bytes 隨機金鑰，Base64 後設定 `ACCOUNT_KEY_SECRET`，不可與 `JWT_SECRET` 共用。它用於 AES-GCM 加密資料庫中的 RSA 私鑰，不提供給前端。務必安全備份並保持重啟後一致，遺失或直接更換會使既有私鑰無法解開；輪換時需另做資料遷移。

PowerShell 可用以下方式在目前終端產生私鑰加密主金鑰（啟動 Java 程式需使用同一環境；請另外安全保存，不會自動持久化）：

```powershell
$accountKeyBytes = New-Object byte[] 32
$accountKeyRng = [Security.Cryptography.RandomNumberGenerator]::Create()
$accountKeyRng.GetBytes($accountKeyBytes)
$accountKeyRng.Dispose()
$env:ACCOUNT_KEY_SECRET = [Convert]::ToBase64String($accountKeyBytes)
```

預設 `security.auth.require-https=true`。普通 HTTP 會導向 HTTPS，但應用程式不會自動產生憑證或 HTTPS listener，前端必須直接使用 HTTPS URL，不能先把帳密送往 HTTP 再依賴重新導向。

僅限隔離的本機開發環境，可明確設定 `SECURITY_AUTH_REQUIRE_HTTPS=false` 暫時使用 HTTP。此模式不提供傳輸加密。

## 註冊

`POST /auth/register`：

```json
{"account":"parent01","name":"家長","password":"abc12345"}
```

註冊時尚無帳號專屬公鑰，因此初始密碼以 HTTPS 傳送。後端產生隨機 RSA 2048-bit 金鑰對，public_key 儲存 SPKI DER 的標準 Base64，private_key 儲存 `v1:` 開頭的 AES-GCM 加密 PKCS8 私鑰（含隨機 IV 與驗證標籤），並以公鑰作為 AAD 綁定。密碼保存 BCrypt 雜湊，註冊成功不直接發行 JWT。

`token` 目前為保留欄位，註冊時填入空字串以符合 NOT NULL；不保存 JWT，也不改成同帳號單一登入。密碼、私鑰與 token 欄位不會經由 Account 的 JSON 或 toString 輸出。

## 登入前查公鑰

`POST /auth/public-key`：

```json
{"account":"parent01"}
```

成功回應的 data 只有 `publicKey`、`algorithm: RSA-OAEP-256`、`keyFormat: SPKI`，絕不回傳私鑰。找不到帳號、重複帳號或沒有公鑰時回傳統一認證失敗。本端點依設計仍可由成功/失敗辨識帳號存在性，部署時應限制查詢頻率；此版本沒有加入防列舉機制。

## 登入

`POST /auth/login`，Content-Type 為 `application/json`：

```json
{"account":"parent01","password":"使用該帳號公鑰加密後的標準 Base64 RSA 密文"}
```

`password` 欄位現在必須是密文，已不接受原始密碼。前端使用 Web Crypto 的 RSA-OAEP / SHA-256，MGF1 也必須為 SHA-256、label 為空。後端取出並解開 private_key，RSA 解密成功後再以 BCrypt 驗證，不會把密碼雜湊後直接當密碼使用。詳細前端程式見 `superpowers/examples/auth-client.js`。

每次 OAEP 加密都有隨機性，但相同密文仍可重送；本版本未加入單次 challenge 防重放，不能取代 HTTPS。既有帳號若尚未有有效公私鑰，不能使用此新流程登入；程式不會在查公鑰時擅自更換金鑰。

成功回應的 `data` 包含 `accessToken`、`tokenType`、`expiresIn`（預設 900 秒）、`expiresAt`、`role`。

後續請求帶入 `Authorization: Bearer <accessToken>`。每次請求都有不同的 UUID `traceId`，回應 JSON、`X-Trace-Id` 及 MDC 日誌使用相同值。

## API 權限資料

關聯：`account.role -> role.id -> role_menu -> menu -> menu_api -> api`。

`api.method` 固定代碼：1 GET、2 POST、3 PUT、4 DELETE、5 PATCH、6 HEAD、7 OPTIONS。

`api.path` 使用從 `/` 開始的應用程式路徑（不含 context path、query string），支援 Ant 路徑，例如 `/accounts/*` 或 `/accounts/**`。以整個路徑比對，不使用字串前綴比對。只授權明確綁定的選單，不自動繼承父子選單權限。GET 不會自動授予 POST 或 HEAD。

只有 POST `/auth/register`、`/auth/public-key`、`/auth/login` 不需要 Token，其餘請求都必須有有效 Token 及資料庫權限。沒有任何管理員預設繞過規則。角色異動、帳號或角色移除會使原 Token 的授權被拒絕；角色選單/API 異動每次查詢即時生效。

Token 有簽章，並非加密容器；客戶端可以解讀角色，但無法在不知道簽章金鑰的情況下修改後繼續通過驗證。修改簽章金鑰會使所有已發行 Token 失效。目前不提供 refresh token 或個別登出撤銷清單。

## 失敗

- 帳密錯誤、原始密碼、錯誤金鑰或損壞密文：HTTP 401，code 10006，統一訊息。
- Token 缺少、過期、格式不正確或簽章無效：HTTP 401，code 10005。
- 角色無對應路徑/method 權限：HTTP 403，code 10004，訊息「權限不足」。
- 既有 Controller 參數與業務例外仍沿用原本 ApiResult 格式。

## 驗證

JDK 8 下執行 `mvn test`，MockMvc 測試以 mock Service 驗證登入與完整 Security FilterChain；另以隔離 H2 資料庫驗證實際 MyBatis-Plus Service 查詢，包括重複帳號、角色存在性、失效選單、空關聯、去重與 HTTP method 過濾。

設定 `AUTH_DB_TEST=true` 後執行 `mvn test`，會額外啟用唯讀 MySQL JDBC / Service 整合測試。該測試不新增、修改或刪除任何 MySQL 資料。測試用帳號只會建立在 H2 隔離資料庫；真實使用前仍需先配置角色、唯一索引、環境主金鑰及 HTTPS。

查詢由 `AccountServiceImpl.findLoginAccounts`、`AccountServiceImpl.findCurrentRole` 與 `ApiServiceImpl.findApis` 實作。Mapper 僅繼承 `BaseMapper`，不包含手寫 SQL。權限查詢拆為數次 MyBatis-Plus Lambda 查詢，空關聯立即回傳空結果。

參考：[Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)、[BCryptPasswordEncoder](https://docs.spring.io/spring-security/site/docs/5.7.4/api/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.html)、[Java 8 OAEPParameterSpec](https://docs.oracle.com/javase/8/docs/api/javax/crypto/spec/OAEPParameterSpec.html)。
