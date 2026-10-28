# DATABASE SCHEMA

Flyway migration: `V1__init.sql`

Tables:
- `users`
- `websites`
- `check_results`
- `incidents`
- `alert_deliveries`

All timestamp columns are UTC `TIMESTAMP WITH TIME ZONE`.

Key columns:
- `websites.claim_until`: short lease to prevent duplicate concurrent checks across instances
- `websites.expected_statuses`: comma-separated expected HTTP status codes
- `incidents.fingerprint`: groups repeated compatible failures
- `alert_deliveries.dedup_key`: idempotency key for alert emission
