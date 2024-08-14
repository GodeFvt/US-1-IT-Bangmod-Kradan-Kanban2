FROM mysql/mysql-server

COPY . /docker-entrypoint-initdb.d/

HEALTHCHECK --interval=2s --timeout=2s --retries=20 CMD /healthcheck.sh