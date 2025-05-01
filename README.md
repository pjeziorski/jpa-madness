# JPA Madness

It is to test various JPA mechanisms using Spring Boot

## Learnings

- h2 does not fully support Serializable Isolation Level  
  In order to test it properly use real db (eg. postgres)
- Postgres for Serializable needs SELECT on same table
- MSSQL Serializable actually orders and blocks transactions


## Tools

Postgres on Docker:

docker run --name postgres-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres

MSSQL on Docker:

docker run --name mssql-db -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=P@ssw0rd" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2022-latest