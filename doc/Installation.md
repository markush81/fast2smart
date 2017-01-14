## Install 

- Install [FastData-Cluster](https://github.com/markush81/fastdata-cluster)
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

SSH into one of the kafka servers:

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

We need to create a keyspace as well as the needed tables. Change into `fast2smart`.

```bash
../apache-cassandra-3.9/bin/cqlsh -f spark/src/cassandra/00_create_keyspace.cql
../apache-cassandra-3.9/bin/cqlsh -f spark/src/cassandra/01_create_tables.cql
```

### Let's play!

[Run any Play](Run.md)