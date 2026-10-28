# DEPLOYMENT

## Environment
Use variables from `.env.example`.

Required for production:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `SESSION_COOKIE_SECURE=true`
- Mail settings (`MAIL_HOST`, etc.) if email alerts are desired

## Container
```bash
mvn -DskipTests package
docker build -t sitesentinel .
```

## Compose
```bash
docker compose up --build
```
