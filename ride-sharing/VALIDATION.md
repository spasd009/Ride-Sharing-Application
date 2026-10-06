# Validation scope

Maven integration tests use the real Spring controllers, security and transaction service with an isolated H2 database in MySQL mode. This checks application behavior but does not substitute for running the documented isolated MySQL tests. Production uses MySQL Connector/J; H2 remains test-only and is excluded from the runnable JAR.

Privacy tests cover security headers, sensitive response contents, cross-account isolation, bounded password input and safe error handling. Original registration/login, CSRF, rendering and business workflow tests remain included. Ride sharing also tests concurrent reservation contention and booking ownership. No real MySQL server, public deployment, graphical browser or external integration is verified in this environment.

Latest result: 9 tests passed, 0 failures, 0 errors, 0 skipped. Configuration/SQL/build files and H2 console URLs returned 404 for an authenticated user. Package checks confirmed environment credential placeholders, MySQL driver and no H2 runtime driver. Final CSS polish was packaged after the passing tests without repeating backend tests.
