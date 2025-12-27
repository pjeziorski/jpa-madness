# JPA Madness

It is to test various JPA mechanisms using Spring Boot

## How to Run

It is mainly a collection of Unit Tests that are proving different mechanisms of JPA.  

In order to run it, you need to:
- go to jpa-madness-app/main/resources
- copy local-config-template.properties to local-config.properties
- configure local-config.properties according to your needs

## Use Cases

#### UC1: Constraints Tests

- Tests constraints (like not null) on database and on hibernate level

### UC2: Entity References Tests

- Tests what references are returned on hibernate methods
- Liquibase create via xml

### UC3: One to Many Dependencies Tests

- Tests saving One to Many Dependencies with different directions
- TODO: Check Use Cases

### UC4: One to One Dependencies Tests

TODO

### UC5: Many to Many Dependencies Tests

TODO

### UC6: Eager Dependencies Tests


### UC_TODO

- sequences
- flush / no flush
- Mialem chyba zamiar zrobic podzial na zapis i odczyt ale rezygnuje (27.12.2025)

## Learnings

### Save vs SaveAndFlush

- save() writes to database at the commit of the transaction
- saveAndFlush() writes to database immediately but does commit the transaction
- because saveAndFlush() writes to database, it validates database constraints

References:
- persistance.contraints

### Isolation Levels

#### Anomalies:

- Dirty Reads - reading uncommitted rows
- Lost Update - When changes of one transaction are not visible in other transaction
- Repeatable Read - while reading same row twice, the data change
- Phantom Read - while making same query twice, new row appear

| Occurrence       | Dirty Reads | Lost Update | Repat. Read | Phantom Read |
|------------------|-------------|-------------|-------------|--------------|
| READ_UNCOMMITTED | Yes         | Yes         | Yes         | Yes          |
| READ_COMMITTED   | No          | Yes         | Yes         | Yes          |
| REPEATABLE_READ  | No          | No          | No          | Yes          |
| SERIALIZABLE     | No          | No          | No          | No           |


#### Engine notes:

Postgres:

- Phantom Reads are not occurring due to MVCC  
  https://github.com/acakojic/postgresql-learning/blob/main/transactions/isolation-levels/1_13_postgresql_repeatable_read_transactional_isolation_level.md

REMOVE:
- h2 does not fully support Serializable Isolation Level  
  In order to test it properly use real db (eg. postgres)
- Postgres for Serializable needs SELECT on same table
- MSSQL Serializable actually orders and blocks transactions

### Unit Testing

In case you want to test multiple transactions insied single unit test and you expect to run them independently,
you need to add annotation @Transactional(propagation = Propagation.NOT_SUPPORTED)
See: https://stackoverflow.com/questions/27987097/disabling-transaction-on-spring-testing-test-method

See: UnitTestsTransactionsTest

## Tools

Postgres on Docker:

docker run --name postgres-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres

MSSQL on Docker:

docker run --name mssql-db -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=P@ssw0rd" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2022-latest