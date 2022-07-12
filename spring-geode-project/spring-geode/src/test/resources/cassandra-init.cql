CREATE KEYSPACE IF NOT EXISTS CustomerService WITH replication = { 'class':'SimpleStrategy', 'replication_factor':1 };
CREATE TABLE IF NOT EXISTS CustomerService.Customers (id BIGINT PRIMARY KEY, name TEXT);
CREATE INDEX IF NOT EXISTS CustomerNameIdx ON CustomerService.Customers(name);
INSERT INTO CustomerService.Customers (id, name) VALUES (16, 'Pie Doe');
