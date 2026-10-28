# TESTING

Run all tests:
```bash
mvn test
```

Current tests cover:
- URL normalization/validation
- Host SSRF blocking and safe hosts
- website ownership enforcement
- incident grouping/recovery threshold logic

Integration tests use H2 (PostgreSQL mode) with Flyway migrations.
