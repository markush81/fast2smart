## Install 

- Install [FastData-Cluster](https://github.com/markush81/fastdata-cluster/tree/spark-cluster)
  - if you want to use local only see [here](https://github.com/markush81/fast2smart/tree/run-local).
- Clone [fast2smart](https://github.com/markush81/fast2smart) repository

#### Fast Data Cluster

Follow instructions [here](https://github.com/markush81/fastdata-cluster/blob/master/README.md).

#### Clone fast2smart repository

```bash
git clone https://github.com/markush81/fast2smart
```

## Setup

For further steps following directory layout is assumed:

```bash
fastdata-cluster
fast2smart
```

#### Apache Kafka

SSH into one of the kafka nodes:

```bash
cd fastdata-cluster
vagrant ssh kafka-1 

lucky:fastdata-cluster markus$ vagrant ssh kafka-1
Last login: Mon Jan  2 12:35:53 2017 from 10.0.2.2
[vagrant@kafka-1 ~]$
```

Create all topics:

```
kafka-topics.sh --create --zookeeper zookeeper-1:2181 --replication-factor 2 --partitions 6 --topic enrolments
kafka-topics.sh --create --zookeeper zookeeper-1:2181 --replication-factor 2 --partitions 6 --topic purchases 
kafka-topics.sh --create --zookeeper zookeeper-1:2181 --replication-factor 2 --partitions 6 --topic treatments  
```

Check if topics have been created correctly:

```
[vagrant@kafka-1 ~]$ kafka-topics.sh --zookeeper zookeeper-1:2181 --describe --topic enrolments
Topic:enrolments	PartitionCount:6	ReplicationFactor:2	Configs:
	Topic: enrolments	Partition: 0	Leader: 2	Replicas: 2,3	Isr: 2,3
	Topic: enrolments	Partition: 1	Leader: 3	Replicas: 3,1	Isr: 3,1
	Topic: enrolments	Partition: 2	Leader: 1	Replicas: 1,2	Isr: 1,2
	Topic: enrolments	Partition: 3	Leader: 2	Replicas: 2,1	Isr: 2,1
	Topic: enrolments	Partition: 4	Leader: 3	Replicas: 3,2	Isr: 3,2
	Topic: enrolments	Partition: 5	Leader: 1	Replicas: 1,3	Isr: 1,3
```

#### Apache Cassandra

1. Copy cql scripts to `fastdata-cluster/exchange` folder.
2. SSH into one of the analytics nodes:

```bash
lucky:fastdata-cluster markus$ vagrant ssh analytics-1
Last login: Mon Jan  2 12:35:53 2017 from 10.0.2.2
[vagrant@analytics-1 ~]$ 
```

Execute both scripts:

```bash
cqlsh -f /vagrant/exchange/00_create_keyspace.cql
cqlsh -f /vagrant/exchange/01_create_tables.cql 

```

Check if everything has been done right:

```bash
[vagrant@analytics-1 ~]$ cqlsh -e "DESCRIBE KEYSPACE fast2smart;"

CREATE KEYSPACE fast2smart WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE fast2smart.member_delta_balance (
    member bigint,
    year int,
    month int,
    amount bigint,
    maxdate timestamp,
    PRIMARY KEY (member, year, month)
) WITH CLUSTERING ORDER BY (year ASC, month ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

CREATE TABLE fast2smart.member_monthly_balance (
    member bigint,
    year int,
    month int,
    amount bigint,
    maxdate timestamp,
    PRIMARY KEY (member, year, month)
) WITH CLUSTERING ORDER BY (year ASC, month ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';
```

### Let's play!

[Run any Play](Run.md)