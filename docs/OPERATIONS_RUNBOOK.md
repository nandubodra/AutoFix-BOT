# OPERATIONS RUNBOOK

## Health
- `GET /actuator/health`
- readiness/liveness are enabled under Spring health probes

## Common checks
- Confirm due checks advancing (`websites.next_check_at`)
- Review latest `check_results` for failures
- Review open incidents and failed `alert_deliveries`

## If email unavailable
Monitoring and incident persistence continue.
Alert records move to retry/fail states with sanitized error summaries.
