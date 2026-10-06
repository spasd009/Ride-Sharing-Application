# Ride Sharing — independent Java + MySQL project

Updated edition: separate MySQL projects, private account data and distinct themes.

Java 17+, Spring Boot, Thymeleaf, Spring Security, JDBC and MySQL. This project runs independently: it does not call the other project and has its own database, user accounts, session cookie, Maven build and launcher. No Node build is needed.

## Windows / VS Code setup

1. Install Java 17 or 21 and MySQL Server 8.0.16+ (MySQL 8.4 recommended). MySQL Workbench is useful for setup. Start the MySQL service.
2. Open `mysql-setup.sql` in Workbench as an administrator. Replace both password placeholders with the same strong application password, then run it. It creates `ride_sharing` and an optional isolated `ride_sharing_test` database.
3. Open this project folder in VS Code. For source edits, install Extension Pack for Java, Spring Boot Extension Pack and Apache Maven 3.9+.
4. Set credentials in the SAME PowerShell terminal that will start Java:

```powershell
$env:DATABASE_USER = "ride_sharing_app"
$env:DATABASE_PASSWORD = "YOUR_PASSWORD_FROM_mysql-setup.sql"
java -jar ride-sharing-1.0.0.jar
```

The download includes the prebuilt JAR. You can instead use `cmd /c run-windows.bat` from this configured terminal. Double-clicking the BAT alone does not inherit variables from another terminal.

5. Open http://localhost:8081, register and sign in. There are no default accounts.

For source development:

```powershell
mvn clean verify
mvn spring-boot:run
```

`mvn verify` runs isolated application tests without needing a MySQL server. H2 is a **test-only dependency**; the production JAR uses MySQL only and requires a configured MySQL server. The app creates its tables on startup; the database/user must already exist. Database persistence belongs to MySQL, so back up that database rather than an application `data` folder.

## Configuration

Default database: `jdbc:mysql://localhost:3306/ride_sharing?connectionTimeZone=LOCAL`.
Default HTTP port: `8081`. Override with `PORT`.
Use `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` to change the connection. If MySQL uses another port, change DATABASE_URL. Departure timestamps in ride sharing use the Java server's local timezone.

## Optional Docker database

Copy `.env.example` to `.env` and replace both passwords, then run `docker compose up -d`. This is an alternative to locally installed MySQL; do not run both on port 3306. Spring Boot does not automatically load `.env`; set PowerShell DATABASE_USER and DATABASE_PASSWORD too. Each project has its own Docker volume. To run both database containers concurrently, use `MYSQL_PORT=3307` for one container and update that application's DATABASE_URL accordingly. Database initialization variables only take effect when Docker creates a new volume.

## Features

Ride route/date search, publishing, one-seat booking, account history and cancellation. Prevents own-ride bookings, duplicate bookings and overselling through transactions and row locks.

Passwords are BCrypt hashed, writes are protected with CSRF tokens, and queries bind parameters. Browser pages escape user text. MySQL uses explicit foreign keys and InnoDB transactions. Username columns use binary collation to keep username case sensitivity consistent.

## Verify against actual MySQL

Tests delete data. Use ONLY the isolated `ride_sharing_test` database created by the setup script, never your live database.

```powershell
$env:TEST_DATABASE_URL = "jdbc:mysql://localhost:3306/ride_sharing_test?connectionTimeZone=LOCAL"
$env:TEST_DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver"
$env:TEST_DATABASE_USER = "ride_sharing_app"
$env:TEST_DATABASE_PASSWORD = $env:DATABASE_PASSWORD
$env:TEST_SCHEMA = "classpath:schema.sql"
mvn clean verify
```

Afterward clear those TEST variables before ordinary tests. The tests use an isolated fixture and assume no unrelated catalogue records in the test database.

## Suggested next changes

Verified driver profiles; passenger counts; driver cancellation and audited booking statuses; maps/routing and consent-based GPS/WebSocket tracking; verified email, password reset and login throttling; explicit timezone selection; payment provider/webhooks and idempotency; Flyway migrations; HTTPS, backups and monitoring.

These are recommendations, not claims that these integrations are already implemented. MySQL is the database choice for this project; no PostgreSQL configuration is included.

## Validation

See VALIDATION.md for checks performed and their limits. This download changes no files in your remote GitHub repository.

## Demonstration

Create a driver account and publish a future ride. In an incognito window create a passenger account, search the route, book a seat and cancel it from Account. Payments, GPS, price comparison and driver messaging are not integrated.

## Private configuration and deployment

For a hidden password prompt, run `./start-windows.ps1` from PowerShell. It asks for the MySQL username and password only when they are missing, passes them to the Java process and restores the parent terminal environment when the application exits. It writes no credential file. If Windows policy does not allow scripts, use the existing documented environment-variable and JAR commands; do not weaken machine policy for this launcher.

No external integration APIs are connected yet. If adding a maps, payment, email or media provider, call it from a Java service and read its secret from the server environment/secret store. Do not place a secret in templates, CSS, browser JavaScript, URLs, screenshots, SQL scripts committed to Git, or the downloadable JAR. `.env.example` contains placeholders only; Spring does not load `.env` automatically.

Keep custom database setup scripts in `mysql-setup.local.sql` (ignored by Git). Do not commit a filled password into the shipped setup template. Review diffs before pushing; ignore rules do not remove secrets already tracked in Git.

Accounts/bookings/watchlists remain server-side and queries restrict account data to the signed-in user. Public ride listings no longer reveal driver login usernames. Intentionally public catalogue/ride details, CSS, form URLs, IDs and CSRF tokens remain browser-visible; hiding them is not an access control. This package includes source/JAR files, so recipient code inspection is also expected. Never embed real credentials in them.

Production: configure HTTPS first, then set `SPRING_PROFILES_ACTIVE=prod`. This turns on secure session cookies and requires HTTPS. For a trusted TLS reverse proxy, configure trusted forwarded-header handling and block direct untrusted connections before enabling it; otherwise redirects can loop. For direct TLS, set `SERVER_SSL_ENABLED=true`, provide an external keystore via `SERVER_SSL_KEY_STORE` and its server-only `SERVER_SSL_KEY_STORE_PASSWORD`. Use `sslMode=VERIFY_IDENTITY` with the database's real hostname and trusted certificate in DATABASE_URL. Certificates, TLS/proxy setup and a running MySQL server are deployment inputs, not included credentials.

Browser security uses a same-origin content policy, no scripts, no external fonts, no framing, no referrer leakage, and no camera/microphone/location permission. Retain these protections; a future GPS integration needs a deliberately revised geolocation policy. All actions retain CSRF checks; private HTML has no-store headers. Sessions expire after 30 minutes. Errors do not return stack traces, SQL details or raw malformed input.

This update does not add payments, live GPS, email reset, admin uploads or login throttling. Those remain the documented next improvements. No production security audit or actual MySQL-server verification has been performed here.
