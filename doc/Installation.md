## Install 

You need to install the following applications:

- [Apache Kafka (0.10.1.0)](https://www.apache.org/dyn/closer.cgi?path=/kafka/0.10.1.0/kafka_2.11-0.10.1.0.tgz)
- [Apache Cassandra (3.9)](http://www.apache.org/dyn/closer.lua/cassandra/3.9/apache-cassandra-3.9-bin.tar.gz)
- Clone [fast2smart](https://github.com/markush81/fast2smart) repository

#### Apache Kafka

Extract:

```
tar -xzf kafka_2.11-0.10.1.0.tgz
```

Run:


```
cd kafka_2.11-0.10.1.0
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
```

#### Apache Cassandra

Extract:

```
tar -xvf apache-cassandra-3.9-bin.tar.gz
```

Run:

```bash
cd apache-cassandra-3.9
bin/cassandra -f
```

#### Clone fast2smart repository

```bash
git clone https://github.com/markush81/fast2smart
```

## Setup

For further steps following directory layout is assumed:

```bash
kafka_2.11-0.10.1.0
apache-cassandra-3.9
fast2smart
```

#### Apache Kafka

There is no need to do anything, because the needed topics will be autocreated. Change into `kafka_2.11-0.10.1.0`.

If you like to do this manually

```bash
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --enrolments
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --purchases 
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --treatments  
```

#### Apache Cassandra

We need to create a keyspace as well as the needed tables. Change into `fast2smart`.

```bash
../apache-cassandra-3.9/bin/cqlsh -f spark/src/cassandra/00_create_keyspace.cql
../apache-cassandra-3.9/bin/cqlsh -f spark/src/cassandra/01_create_tables.cql
```

### Let's play!

[Run any Play](Run.md)