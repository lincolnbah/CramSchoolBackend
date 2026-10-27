# Match ECPay verification to the callback handler flow

**Goal:** ECPay returns PaymentNotifyVO; payment service owns order validation, persistence and acknowledgements.
**Why planning is required:** Financial callbacks and concurrent order updates affect payment state.
**Acceptance:** No new production helper methods. Valid signatures populate amount, fee, third-party ID, payment time/type and result. Invalid signatures/configuration/fields and simulation never fail or pay a real order. Check provider and amount, allow active orders only, prevent terminal-state overwrites, acknowledge completed handling only. Preserve existing Redis duplicate suppression without unconditional deletion. No schema execution or live payment.

### Outcome 1: Handler and existing VO/util contract
- Work: Replace incompatible String returns and undefined gateway in ECPay.verifyNotification inline; fix existing PaymentUtil error helpers to return PaymentNotifyVO with statusEnum; distinguish simulation and pending payment.
- Verify: Official checksum vector, signed successful/failed/pending/simulated callbacks, tampering and malformed values.

### Outcome 2: Match service and persistence
- Work: Correct active-state and decimal comparisons; reject unauthenticated requests without DB mutation; validate provider; check save result; condition writes on original active status. Select gateway by ID, retain selected gateway on generated orders. Ensure form decoding and errors reach service correctly.
- Verify: Focused service tests and H2 competing-terminal-update check.

### Outcome 3: Completion evidence
- Work: Review changes against current callers; describe flow and remaining limits.
- Verify: Maven tests, whitespace check, source review. No live gateway or external DB tests claimed.
