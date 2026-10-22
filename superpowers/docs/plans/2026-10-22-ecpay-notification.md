# ECPay notification verification

**Goal:** Verify ECPay form notifications before any order processing or acknowledgement.
**Why planning is required:** Payment authenticity and acknowledgement affect financial state and provider retries.
**Acceptance:** Reject malformed, tampered and wrong-merchant notifications; distinguish simulation and unsuccessful payment; never acknowledge an unpersisted real notification. No order schema or controller changes.

### Outcome 1: Shared checksum and notification checks
- Work: Per user preference, all verification is inline in verifyNotification, with no added production helper methods and no changes to payment creation. Case-insensitive field ordering, exclude CheckMacValue, reject ambiguous field names. Validate required fields and signed status. Simulated notifications are explicitly ignored; real notifications remain unacknowledged until future order integration verifies amount and saves results idempotently.
- Verify: Focused unit tests with valid signed fixtures, tampering, wrong merchant, malformed fields, simulation, failed payment and unavailable order handling; official checksum example.

### Outcome 2: Evidence and scope review
- Work: Review ECPay diff and controller contract; document missing order handling and no live gateway test.
- Verify: Maven focused tests and git diff --check.
