# Privacy and theme update

- Required environment credentials; no default database username/password is packaged.
- Server-only MySQL configuration; optional hidden password launcher; secret-related ignore rules.
- Removed login usernames from public ride views and limited projected database columns.
- Kept account-scoped booking/watchlist access, transaction safety and CSRF protection.
- Added same-origin CSP, no-referrer and permissions policies; retained anti-framing, no-sniff and no-store defaults.
- Added production HTTPS/secure-cookie profile, bounded forms, session expiry and cookie-only tracking.
- Replaced technical parser messages with safe UI feedback; suppressed stacktrace/binding-error output.
- Guarded documentary media paths against external URLs and corrected BCrypt's UTF-8 byte-length check.
- Distinct themes, button hierarchy, keyboard focus, responsive layout and reduced-motion support.
