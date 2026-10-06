-- Run once as a MySQL administrator in MySQL Workbench.
-- Replace both occurrences of REPLACE_WITH_A_STRONG_PASSWORD before execution.
CREATE DATABASE IF NOT EXISTS ride_sharing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'ride_sharing_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_A_STRONG_PASSWORD';
ALTER USER 'ride_sharing_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_A_STRONG_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES ON ride_sharing.* TO 'ride_sharing_app'@'localhost';
-- Optional isolated database for MySQL integration tests. Tests delete their test data.
CREATE DATABASE IF NOT EXISTS ride_sharing_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES ON ride_sharing_test.* TO 'ride_sharing_app'@'localhost';
