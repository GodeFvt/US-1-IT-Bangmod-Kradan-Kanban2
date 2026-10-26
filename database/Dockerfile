FROM postgres:16-alpine

COPY postgresql.sql /docker-entrypoint-initdb.d/001-schema.sql

HEALTHCHECK --interval=5s --timeout=3s --retries=20 CMD-SHELL pg_isready -U "$${POSTGRES_USER}" -d "$${POSTGRES_DB}"
