# Ride Sharing

A Java 17+ ride-sharing web app built with Spring Boot, Thymeleaf, Spring Security, JDBC, and MySQL. It supports account registration, ride publishing and route/date search, one-seat bookings, account history, and cancellation. It prevents booking your own ride, duplicate bookings, and overselling using transactions and row locks.

## Requirements

- Java 17 or newer
- Maven 3.9+
- MySQL 8.0.16+ (8.4 recommended), or Docker with Compose

## Run locally (Windows PowerShell)

Create the database and application account by running `ride-sharing/mysql-setup.sql` in MySQL Workbench. Replace both `REPLACE_WITH_A_STRONG_PASSWORD` placeholders with the same unique password. The script creates the app database `ride_sharing` and isolated test database `ride_sharing_test`.

From the project directory:

```powershell
cd .\ride-sharing
$env:DATABASE_USER = "ride_sharing_app"
$env:DATABASE_PASSWORD = "YOUR_MYSQL_PASSWORD"
mvn spring-boot:run
```

Open http://localhost:8081 and register. There are no default accounts. Or use `.\start-windows.ps1` for a hidden credential prompt.

## Docker database option

From `ride-sharing`:

```powershell
Copy-Item .env.example .env
# Edit .env and replace both placeholder passwords
docker compose up -d
$env:DATABASE_USER = "ride_sharing_app"
$env:DATABASE_PASSWORD = "YOUR_DATABASE_PASSWORD"
mvn spring-boot:run
```

Spring does not automatically load the Compose `.env` file; set the app credentials in the shell too. Stop MySQL with `docker compose down` (the named volume preserves database files).

## Build and tests

From `ride-sharing`:

```powershell
mvn clean verify
mvn clean package
```

The test suite uses an isolated in-memory H2 database by default; live MySQL is not needed for ordinary tests. For MySQL integration tests, configure the dedicated `ride_sharing_test` database using the variables documented in `ride-sharing/README.md`. **Never run the destructive test suite against production data.**

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:mysql://localhost:3306/ride_sharing?connectionTimeZone=LOCAL` | MySQL connection |
| `DATABASE_USER` | Required | MySQL application user |
| `DATABASE_PASSWORD` | Required | MySQL application password |
| `PORT` | `8081` | HTTP port |

The Spring Boot production profile requires HTTPS. Configure TLS at a trusted reverse proxy or provide a server-side keystore, and set production database TLS to `sslMode=VERIFY_IDENTITY`. Keep credentials and keystores outside source control.

## Security and project notes

Passwords use BCrypt; state-changing forms use CSRF protection; SQL uses bound parameters; private booking/account data is restricted to its account. Sessions expire after 30 minutes. Do not commit filled `.env` files, custom credential SQL, JARs, logs, keystores, secrets, or licensed media. The sample setup SQL and `.env.example` contain placeholders only.

Preview images are in `ride-sharing/previews/`. CI workflow is in `.github/workflows/java.yml`. Detailed setup, isolated MySQL test instructions, validation scope, and future ideas are in [`ride-sharing/README.md`](ride-sharing/README.md).

## Not implemented

Payments, GPS tracking, email/password reset, login throttling, maps, driver-profile verification, and admin ride editing are not included. No production deployment or live MySQL verification is implied by the preview images or unit tests.
