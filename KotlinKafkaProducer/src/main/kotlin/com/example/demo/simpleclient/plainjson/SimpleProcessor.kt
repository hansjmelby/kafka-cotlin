package com.example.demo.simpleclient.plainjson

import com.ippontech.kafkatutorials.simpleclient.Person
import com.example.demo.simpleclient.agesTopic
import com.example.demo.simpleclient.jsonMapper
import com.example.demo.simpleclient.personsTopic
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

// $ kafka-topics --zookeeper localhost:2181 --create --topic ages --replication-factor 1 --partitions 4

fun main(args: Array<String>) {
    SimpleProcessor("localhost:9092").process()
}

class SimpleProcessor(brokers: String) {

    private val logger = Logger.getLogger("SimpleProcessor")
    private val consumer = createConsumer(brokers)
    private val producer = createProducer(brokers)

    private fun createConsumer(brokers: String): Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = "person-processor"
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        return KafkaConsumer<String, String>(props)
    }

    private fun createProducer(brokers: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    fun process() {
        consumer.subscribe(listOf(personsTopic))

        logger.info("Consuming and processing data")

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            logger.info("Received ${records.count()} records")

            records.iterator().forEach {
                val personJson = it.value()
                logger.log(Level.INFO,"JSON data: $personJson")

                val person = jsonMapper.readValue(personJson, Person::class.java)
                logger.log(Level.INFO,"Person: $person")

                val birthDateLocal = person.birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val age = Period.between(birthDateLocal, LocalDate.now()).getYears()
                logger.log(Level.INFO,"Age: $age")

                val future = producer.send(ProducerRecord(agesTopic, "${person.firstName} ${person.lastName}", "$age"))
                future.get()
            }
        }
    }
}
