# Exclude local secrets from Git

**Goal:** Keep local credentials and generated artifacts out of commits while providing a safe configuration template.
**Why planning is required:** Secret exclusion affects disclosure of database credentials and private keys.
**Acceptance:** Local configuration remains unchanged; Git ignores local configuration, environment files, private keys, database dumps and build output; the configuration example remains eligible for commit; no files are staged or pushed.

### Outcome 1: Safe Git defaults and setup instructions
- Work: Add root `.gitignore`, a credential-free `application.example.yml`, and setup instructions.
- Verify: `git check-ignore` for representative sensitive paths and `git status --short --untracked-files=all`; inspect the example for literal credentials and check the index is empty.
