# SECURITY

- Passwords stored with Spring DelegatingPasswordEncoder (BCrypt)
- CSRF is enabled for state-changing operations
- Session cookie set HttpOnly and SameSite=Lax; secure flag configurable
- Basic login throttling blocks repeated failed login attempts per email/window
- URL input validation:
  - HTTPS only
  - no embedded credentials
  - no fragments
  - default HTTPS port only
- SSRF protections:
  - reject loopback/private/link-local/site-local/multicast/reserved ranges
  - block cloud metadata targets
  - revalidate redirect targets
- No arbitrary custom headers/secrets/user credentials for checks
- No response bodies persisted to avoid sensitive content retention
