# kafka-cotlin

# Setting up the enviroment

docker-compose up -d

# Commands

docker-compose exec ksqldb-cli ksql http://ksqldb-server:8088

# KSQL
 
 CREATE STREAM sensor_avro_stream WITH (KAFKA_TOPIC='sensor-avro',VALUE_FORMAT='AVRO');

# Registry URL`s

http://localhost:8081/subjects/sensor-avro-value/versions/latest
http://localhost:8081/subjects/

# shutting down the envoroment
* Stop all applications
* docker-compose down
