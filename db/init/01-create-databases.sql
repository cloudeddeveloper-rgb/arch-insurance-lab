-- Runs once on first Postgres init (see docker-compose volume mount).
-- Owned by POSTGRES_USER (appuser), which each service connects as.
CREATE DATABASE customerdb;
CREATE DATABASE ratingdb;
CREATE DATABASE policydb;
