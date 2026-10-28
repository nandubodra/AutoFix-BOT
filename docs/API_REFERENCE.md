# API REFERENCE

Base: `/api/v1` (authenticated session required)

## Websites
- `GET /websites`
- `POST /websites`
- `PUT /websites/{id}`
- `POST /websites/{id}/pause`
- `POST /websites/{id}/resume`
- `DELETE /websites/{id}`

## Incidents
- `GET /incidents?page=0&size=20`
- `POST /incidents/{id}/ack`

## Reports
- `GET /reports/websites/{websiteId}/summary?from=<ISO-8601>&to=<ISO-8601>`

Errors are sanitized and consistent:
```json
{"code":"BAD_REQUEST","message":"...","path":"...","timestamp":"..."}
```
