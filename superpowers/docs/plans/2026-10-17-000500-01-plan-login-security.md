# 登入與角色 API 授權

**Goal:** 在 JDK 8 / Spring Boot 2.7 建立 Controller、DTO、Service、資料庫角色授權與防竄改的 15 分鐘 JWT。
**Why planning is required:** 新增身分認證、密碼處理及 API 存取控制。
**Acceptance:** 正確帳密可取得帶角色的 Token；錯誤帳密、過期或竄改 Token 回傳 401；無角色 API 權限回傳 403；相同路徑不同 method 分開驗證；所有回應包含 MDC traceId；無授權資料預設拒絕。

### Outcome 1: 確認帳密與資料契約
- Work: 未收到加密方式偏好回覆，依已告知的預設採 HTTPS，預設強制安全通道，不額外實作 RSA 欄位加密。資料庫使用 BCrypt，不允許明文比對；提供 password varchar(255) 遷移 SQL，不自動執行。API method 建立明確常數，現有 API 表為空。登入失敗不洩漏帳號存在與否。
- Verify: 檢視 schema、DTO 驗證、密碼雜湊測試。

### Outcome 2: 登入及簽章 Token
- Work: Spring Security resource server 驗證 JWT；固定 HS256、伺服器設定至少 256-bit 簽章金鑰、issuer、subject、role、iat、exp，預設 15 分鐘；金鑰缺失時拒絕啟動，不提供固定預設密鑰。僅開放明確登入端點，其餘需認證。
- Verify: JDK 8 Maven 測試涵蓋成功、錯誤帳密、過期、竄改、錯誤簽章與非預期演算法。

### Outcome 3: 每次請求授權及錯誤回應
- Work: 依 account -> role -> role_menu -> menu -> menu_api -> api 查詢授權，以路徑與 HTTP method 比對；每次查 DB 並確認目前角色與 Token 一致。Security 層 401/403 回傳 ApiResult 與 traceId。禁止 Session 與表單登入。
- Verify: MockMvc 測試未登入、有效 Token、跨 method 拒絕、角色變更、無對應 API；驗證 JSON 與回應標頭追蹤碼。

### Outcome 4: 整合交付
- Work: 修正既有 JDBC URL 尾端多餘大括號；提供啟動環境設定、資料初始化及前端呼叫說明。保留既有資料，不自動建立帳號或角色權限。
- Verify: JDK 8 全套測試、編譯與安全設定檢視；區分 Mock 驗證與真實資料庫驗證。
