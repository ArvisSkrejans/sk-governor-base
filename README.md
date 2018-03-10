SK Governor Base
========

Universal "smart <anything>" back-end governor modules. Self-educational Scala/Akka project using Actor pattern. When completed will support sensor data mining, storing and processing.

### Install and run

- To compile and test run from sbt, run "sbt run"
- To compile install as a Debian package run "sbt debian:packageBin
". There will be sk-governor-base_0.1_all.deb created under  target dir. Install using apt-get and run with "sk-governor-base" from terminal.


### Current status

- AKKA Actor system that initializes sensors based on defined config.
- Sample setup for testing - two dummy temperature and humidity sensor drivers. Config has template layout using these sensors for various rooms.
- Data is saved in Kafka (requires a running instance).
- Background task processes data and sends it to Elasticsearch for seperate temperature and humidity daily logs.

### Requirements

- Apache Kafka - running background service with default settings. Example: 
``` sudo /opt/kafka/kafka_2.12-1.0.0/bin/kafka-server-start.sh /opt/kafka/kafka_2.12-1.0.0/config/server.properties ```
- Elasticsearch background service on port 9200.

### TO-DO

- Implement drivers for various real sensors.
- Implement db layer for configuration options.
- Improve installation options with dependencies and background setup tasks.
- Implement basic sensor data processing with various profiles.