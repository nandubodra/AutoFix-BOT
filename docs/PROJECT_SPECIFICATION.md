# PROJECT SPECIFICATION

Implemented MVP scope:
- Spring Boot + Thymeleaf + PostgreSQL + Flyway foundation
- User registration/login with secure password hashing and server sessions
- Website CRUD with per-user ownership enforcement and pause/resume
- HTTPS well-known token ownership verification flow
- Persistent monitoring scheduler with bounded worker pool and DB-based claim mechanism
- Safe HTTP checks (timeouts, redirects, response-size cap, SSRF checks)
- Check result persistence with timestamps and failure classification
- Threshold-based incident open/recovery lifecycle and grouping by fingerprint
- Email alert delivery records with dedup and bounded retries
- Dashboard and incident views + REST endpoints under `/api/v1`
- Unit/integration tests for key validation and authorization paths

Planned (not implemented in MVP):
- DNS verification
- richer reporting/aggregation pipelines
- horizontal scaling coordination beyond current claim model
