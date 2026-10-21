# Development schema initialization

Goal: Initialize the seven user-supplied MySQL tables and supplied development seed data on application startup.
Risk trigger: Database schema initialization.
Acceptance: Preserve supplied definitions, add IF NOT EXISTS and statement terminators, use existing spring.sql.init settings, do not drop/alter tables or run against a database. Seed the supplied two accounts, four roles and sandbox gateway only when neither their ID nor natural identifier exists. Keep supplied password hashes and RSA key pairs; omit stale/mismatched JWTs (token NULL). Do not overwrite existing records.
Verification: Check seven named CREATE TABLE statements and separators; inspect existing startup settings and scoped diff. Existing tables are not repaired or migrated. Live MySQL execution is outside this change.
