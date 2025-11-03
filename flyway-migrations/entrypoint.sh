#!/bin/sh

set -e

echo "Running migrations for GATEWAY DB..."
flyway -baselineOnMigrate=true \
  -url=jdbc:postgresql://postgres-gateway:5432/gateway \
  -user=user -password=pass \
  -locations=filesystem:/flyway/sql/gateway migrate

echo "Running migrations for PETS DB..."
flyway -baselineOnMigrate=true \
  -url=jdbc:postgresql://postgres-pets:5432/pets \
  -user=user -password=pass \
  -locations=filesystem:/flyway/sql/pets migrate

echo "Running migrations for OWNERS DB..."
flyway -baselineOnMigrate=true \
  -url=jdbc:postgresql://postgres-owners:5432/owners \
  -user=user -password=pass \
  -locations=filesystem:/flyway/sql/owners migrate

echo "All migrations completed successfully."
