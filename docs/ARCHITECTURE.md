# ARCHITECTURE

## Components
- Web UI: Thymeleaf/Bootstrap pages for auth, dashboard, websites, incidents
- API: `/api/v1/websites`, `/api/v1/incidents`, `/api/v1/reports`
- Persistence: Spring Data JPA + Flyway on PostgreSQL
- Monitoring engine:
  - scheduler selects due websites
  - claims due row via conditional update (`claim_until`)
  - submits checks to bounded executor
  - persists check result and updates thresholds/status
- Incident service:
  - groups failures by fingerprint
  - opens/updates/resolves incidents
  - triggers alert workflow
- Alert service:
  - persists delivery records independent of incident state
  - dedups by `incidentId:type`
  - retries failed sends with bounded attempts/backoff

## Concurrency and safety
- In-process overlap prevented via `inFlight` set
- Cross-process safety best-effort via DB claim window (`claim_until`)
