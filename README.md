# CramSchool Backend

## Local configuration

The actual `src/main/resources/application.yml` is local and ignored by Git.
Existing local configuration is preserved. For a fresh checkout, copy the template:

```powershell
Copy-Item src/main/resources/application.example.yml src/main/resources/application.yml
```

Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, and `ACCOUNT_KEY_SECRET` in the
application process environment. See `superpowers/docs/authentication.md` for
authentication setup. Spring Boot does not automatically load `.env` files.
Never put real credentials in the example file.

## Before committing

`.gitignore` excludes local Spring configuration, environment files, private keys,
IDE files, build output, and common database backup paths. Source SQL migrations
remain eligible for commits. Keep database exports under `dumps/` or `backups/`.

Review staged content before every commit:

```powershell
git status
git diff --cached
```

Ignore rules do not inspect file contents and do not protect files already tracked
by Git. If a secret has already been pushed, revoke or rotate it; adding an ignore
rule does not remove it from Git history.
