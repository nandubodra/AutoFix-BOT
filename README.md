# SiteSentinel MVP

SiteSentinel is a Spring Boot MVP for continuously monitoring owned HTTPS websites, tracking incidents, and recording alert delivery.

## Quick start

### Requirements
- Java 17
- Maven 3.9+
- PostgreSQL 15+

### Local run
1. Copy `.env.example` values into your environment (or use defaults).
2. Start PostgreSQL (or use Docker Compose):
   ```bash
   docker compose up -d postgres
   ```
3. Run app:
   ```bash
   mvn spring-boot:run
   ```
4. Open `http://localhost:8080`, register, add a website, and verify ownership.

### Build and test
```bash
mvn clean verify
```

## Ownership verification (MVP)
1. Add website in UI.
2. Publish the generated token at:
   `https://<your-site>/.well-known/sitesentinel-verification/<token>`
3. Click **Verify ownership**.

This confirms control of site content at a well-known path. It does **not** prove DNS/domain registrar ownership and is intentionally scoped to MVP.

## Security highlights
- Server-side sessions with Spring Security
- Delegating password encoder (BCrypt)
- CSRF protection for web/API state-changing requests
- Login attempt throttling
- Ownership enforcement on all website/incident operations
- HTTPS-only normalized URL validation
- SSRF protections block loopback/private/link-local/reserved/metadata endpoints
- Sanitized API errors; no sensitive data logging

## Actuator
- `GET /actuator/health`
- Readiness/liveness probes enabled via Spring Boot health probes

## Deferred/non-MVP
- Multi-channel notifications
- Advanced SLA analytics
- DNS TXT verification
- Distributed worker queue across many instances

See `docs/` for architecture, schema, API, security, testing, deployment and operations details.
