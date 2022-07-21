CREATE KEYSPACE IF NOT EXISTS CustomerService WITH replication = { 'class':'SimpleStrategy', 'replication_factor':1 };
USE CustomerService;
CREATE TABLE IF NOT EXISTS "Customers" (id BIGINT PRIMARY KEY, name TEXT);
CREATE INDEX IF NOT EXISTS CustomerNameIdx ON "Customers"(name);
