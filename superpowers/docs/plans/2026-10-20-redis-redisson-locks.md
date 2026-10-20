# Redisson lock integration

Goal: Keep RedisTemplate data operations and delegate lock acquisition/release to Redisson.
Planning trigger: Distributed concurrency ownership and lease semantics change.
Acceptance: Fixed lease, immediate acquisition attempt, thread-owned release, shared standalone Redis connection settings, separate lock namespace; no changes to String/Hash operations.

Implementation: Add standalone Redisson client with shutdown lifecycle, replace token lock API with thread-owned API, and verify callers and focused ownership/interruption tests.
Verification: Maven focused tests if available; otherwise report that compilation and runtime behavior remain unverified. Review scoped diff and check whitespace.
