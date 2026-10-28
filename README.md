# SiteSentinel MVP

SiteSentinel is a continuously running website monitoring platform built with **Java + Spring Boot + Thymeleaf + PostgreSQL**.
It lets each user register, add their own websites, verify ownership, monitor uptime/latency, track incidents, and record email alert delivery attempts.

---

## 1) What is implemented right now (MVP)

### Core foundation
- Spring Boot application with Maven build
- Flyway-managed PostgreSQL schema
- Session-based authentication for server-rendered UI
- Actuator health endpoint (`/actuator/health`)
- Dockerfile + `compose.yaml` for local app/database setup

### Accounts and ownership
- User registration and login
- Password hashing with Spring DelegatingPasswordEncoder (BCrypt)
- Login throttling for repeated failures
- Website ownership enforcement on all website/incident actions
- CSRF protection for state-changing requests

### Website management
- Add/list/detail/delete website
- Pause/resume monitoring
- Configurable interval, timeout, expected status codes, thresholds
- HTTPS URL normalization and validation

### Ownership verification (MVP method)
- Well-known token flow:
  `https://<your-site>/.well-known/sitesentinel-verification/<token>`
- Verification confirms site content control at that URL path
- DNS/registrar-level proof is **not** included in MVP

### Monitoring engine
- Persistent schedule fields in database (`last_check_at`, `next_check_at`, etc.)
- Scheduler fetches due sites and submits checks to bounded executor
- DB lease/claim pattern (`claim_until`) to reduce duplicate work in multi-instance scenarios
- In-process overlap protection per website
- HTTPS checks with timeout limits, redirect validation, and response size cap
- SSRF protections block loopback/private/link-local/reserved/metadata destinations

### Incident and alerts
- Incident lifecycle: `OPEN -> ACKNOWLEDGED -> RESOLVED`
- Failure grouping by fingerprint
- Occurrence count + timestamps + check references
- Alert delivery records persist independently from incident persistence
- Email alert deduplication and bounded retry metadata/backoff
- If mail is not configured, monitoring still continues and delivery failure is recorded safely

### Dashboard and APIs
- Dashboard summary for total/healthy/incident/paused websites
- Website detail with recent checks and verification status
- Incident list/detail + acknowledge action
- REST endpoints under `/api/v1` for websites/incidents/reports

---

## 2) Tech stack

- **Java:** 17
- **Framework:** Spring Boot 3
- **View:** Thymeleaf + Bootstrap
- **Database:** PostgreSQL
- **Migrations:** Flyway
- **Security:** Spring Security (session auth)
- **ORM:** Spring Data JPA / Hibernate
- **Mail:** Spring Mail

---

## 3) Local setup

## Prerequisites
- Java 17
- Maven 3.9+
- PostgreSQL 15+ (or Docker)

### Option A: Run with local PostgreSQL
1. Create DB/user or reuse defaults from `.env.example`
2. Export environment variables (or pass via IDE/run config)
3. Start app:
   ```bash
   mvn spring-boot:run
   ```
4. Open: `http://localhost:8080`

### Option B: Run with Docker Compose
1. Build jar first:
   ```bash
   mvn clean package
   ```
2. Start services:
   ```bash
   docker compose up --build
   ```
3. Open: `http://localhost:8080`

---

## 4) Environment variables

Use `.env.example` as the source of truth.

### Database
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

### App/session
- `APP_BASE_URL`
- `SESSION_COOKIE_SECURE`

### Monitoring tuning
- `MONITOR_SCHEDULER_DELAY_MS`
- `MONITOR_CLAIM_SECONDS`
- `MONITOR_THREAD_POOL_SIZE`
- `HTTP_MAX_REDIRECTS`
- `HTTP_MAX_RESPONSE_BYTES`

### Security
- `LOGIN_WINDOW_SECONDS`
- `MAX_LOGIN_ATTEMPTS`

### Email (optional)
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`

---

## 5) First-time usage flow (easy walkthrough)

1. Open app and register user
2. Login
3. Go to **Websites** and add a site (HTTPS only)
4. Copy generated verification token from website detail page
5. Publish token at:
   `/.well-known/sitesentinel-verification/<token>` on your site
6. Click **Verify ownership**
7. Keep monitoring enabled and wait for scheduled checks
8. Check dashboard and incidents page for status changes

---

## 6) API quick reference

Base path: `/api/v1` (authenticated session required)

### Websites
- `GET /api/v1/websites`
- `POST /api/v1/websites`
- `PUT /api/v1/websites/{id}`
- `POST /api/v1/websites/{id}/pause`
- `POST /api/v1/websites/{id}/resume`
- `DELETE /api/v1/websites/{id}`
- `GET /api/v1/websites/{id}/checks?page=0&size=20`

### Incidents
- `GET /api/v1/incidents?page=0&size=20`
- `POST /api/v1/incidents/{id}/ack`

### Reports
- `GET /api/v1/reports/websites/{websiteId}/summary?from=<ISO8601>&to=<ISO8601>`

Error response shape:
```json
{"code":"BAD_REQUEST","message":"...","path":"...","timestamp":"..."}
```

---

## 7) Security notes

- Password hashes only (no plain password storage)
- Session cookie is HttpOnly and SameSite Lax (`secure` configurable)
- CSRF enabled for form/API mutation flows
- Input validation and sanitized API error messages
- No arbitrary outbound headers/credentials for check requests
- Response bodies are not persisted in check results
- Monitoring runs with bounded concurrency to reduce overload risk

---

## 8) Build, test, health

### Build + full verify
```bash
mvn clean verify
```

### Run tests only
```bash
mvn test
```

### Health endpoint
- `GET /actuator/health`

---

## 9) Project docs

Detailed documents are in `/docs`:
- `PROJECT_SPECIFICATION.md`
- `ARCHITECTURE.md`
- `DATABASE_SCHEMA.md`
- `API_REFERENCE.md`
- `SECURITY.md`
- `TESTING.md`
- `DEPLOYMENT.md`
- `OPERATIONS_RUNBOOK.md`
- `CHANGELOG.md`

---

## 10) Honest MVP limitations (planned next)

- DNS TXT ownership verification
- Additional notification channels
- More advanced SLA/reporting analytics
- Broader horizontal scaling and distributed worker strategies

These are intentionally not claimed as completed in current MVP.
