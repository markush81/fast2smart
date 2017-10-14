DROP KEYSPACE IF EXISTS fast2smart;
CREATE KEYSPACE fast2smart WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };