package com.example.demo.simpleclient.withcustomserde

import com.github.javafaker.Faker
import com.ippontech.kafkatutorials.simpleclient.Person
import com.example.demo.simpleclient.personsTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

// $ kafka-topics --zookeeper localhost:2181 --create --topic persons --replication-factor 1 --partitions 4

fun main(args: Array<String>) {
    SimpleProducer("localhost:9092").produce(2)
}

class SimpleProducer(brokers: String) {

    private val logger = Logger.getLogger("SimpleProducer")
    private val producer = createProducer(brokers)

    private fun createProducer(brokers: String): Producer<String, Person> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = PersonSerializer::class.java
        return KafkaProducer<String, Person>(props)
    }

    fun produce(ratePerSecond: Int) {
        val waitTimeBetweenIterationsMs = 1000L / ratePerSecond
        logger.info("Producing $ratePerSecond records per second (1 every ${waitTimeBetweenIterationsMs}ms)")

        val faker = Faker()
        while (true) {
            val fakePerson = Person(
                    firstName = faker.name().firstName(),
                    lastName = faker.name().lastName(),
                    birthDate = faker.date().birthday()
            )
            logger.info("Generated a person: $fakePerson")

            val futureResult = producer.send(ProducerRecord(personsTopic, fakePerson))
            logger.log(Level.INFO,"Sent a record")

            Thread.sleep(waitTimeBetweenIterationsMs)

            // wait for the write acknowledgment
            futureResult.get()
        }
    }
}
