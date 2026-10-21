# ECPay and NewebPay payment services

Goal: Implement hosted one-time checkout and verified notifications in com.school.common.service.payment, without Controller endpoints or database writes.
Planning trigger: Payment integration, cryptographic verification and external contracts.
Acceptance: Existing ECPay/NewebPay services implement a typed payment interface; sandbox by default; credentials externally configured; checkout produces browser POST fields; notifications require signature, merchant, expected order and amount; simulated/unpaid results never count as paid. No external transaction is submitted by this work.

## Outcomes
1. Typed checkout request/form/result and property binding live under the requested package. Existing parent marker interface remains compatible.
2. ECPay AIO V5 uses official sorted .NET-style URL-encoded SHA256 CheckMacValue. NewebPay MPG 2.3 uses AES-256-CBC PKCS7 and SHA256 TradeSha. Initial payment method is credit card one-time payment.
3. Verification includes published cryptographic vectors, tampering/merchant/order/amount failures, sandbox/production URLs, and unpaid/simulated statuses. Compile and run focused tests; isolate pre-existing build failures.

Sources checked 2026-10-21:
- https://developers.ecpay.com.tw/2864/
- https://developers.ecpay.com.tw/2902/
- https://developers.ecpay.com.tw/2878/
- https://www.newebpay.com/website/Page/content/download_api (current NDNF-1.2.6, MPG 2.3)
- https://www.newebpay.com/website/Page/download_file?name=線上交易─幕前支付技術串接手冊_NDNF-1.2.6.pdf

Integration boundary: Browser return is not order settlement. Caller must persist the expected payment/order, handle backend notifications idempotently in a transaction, and acknowledge only after durable processing. Refunds, recurring billing, query and controller routes are outside this initial scope.

## Verification result

- IntelliJ bundled Maven: `mvn -B -ntp -Dtest=PaymentServicesTest test` succeeded; all main sources compiled and 8 tests passed (0 failures/errors/skips).
- Published ECPay CheckMacValue and NewebPay NDNF-1.2.6 AES/SHA256 examples matched exactly.
- Scoped review confirmed no Controller, persistence, real credential, dependency or runtime payment request changes; checkout returns form fields only.
- No merchant sandbox/production transaction or live callback performed; credential setup, endpoint wiring and idempotent order settlement remain the caller's integration responsibilities.
