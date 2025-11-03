FROM flyway/flyway

COPY ./entrypoint.sh /flyway/entrypoint.sh
COPY ./gateway-migrations /flyway/sql/gateway
COPY ./pets-migrations /flyway/sql/pets
COPY ./owners-migrations /flyway/sql/owners

RUN chmod +x /flyway/entrypoint.sh

ENTRYPOINT ["/flyway/entrypoint.sh"]
