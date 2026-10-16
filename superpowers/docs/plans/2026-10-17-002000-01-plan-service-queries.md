# 以 MyBatis-Plus Service 取代 Mapper SQL

**Goal:** Mapper 僅保留 BaseMapper，登入及授權查詢移至對應 ServiceImpl。
**Why planning is required:** 調整身分驗證及多表權限查詢，必須維持原本拒絕規則。
**Acceptance:** AccountService 提供相同帳號查詢與有效角色查詢；ApiService 以角色、選單及 API 關聯查詢相同權限；缺少關聯不擴大授權；Mapper 無 SQL；JDK 8 測試通過。

### Outcome 1: Service 查詢與呼叫端
- Work: 使用 LambdaQueryWrapper / LambdaQueryChainWrapper，登入帳號查詢以 MyBatis-Plus 分頁限制兩筆；角色存在性檢查與多表關聯拆為 Service 查詢；空關聯立即回傳空結果，避免空 IN 條件變全表查詢。使用 RequiredArgsConstructor 注入。
- Verify: 編譯、查找 Mapper SQL 與舊方法引用。

### Outcome 2: 行為驗證
- Work: 調整 Security 測試 mock 的邊界為 Service；唯讀實際資料庫測試改驗證 Service 查詢；使用隔離測試資料庫驗證有資料、重複與失效關聯的結果。
- Verify: JDK 8 `mvn test`，本機 MySQL 測試以 AUTH_DB_TEST=true 明確啟用，不修改本機資料。
