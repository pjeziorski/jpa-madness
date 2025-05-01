# JPA Madness

It is to test various JPA mechanisms using Spring Boot

## Learnings

- h2 does not fully support Serializable Isolation Level  
  In order to test it properly use real db (eg. postgres)


## Install Postgres

Use Docker:

docker run --name postgres-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres