package com.example.demo

import com.github.javafaker.Faker
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.AuthorizationException
import org.apache.kafka.common.errors.OutOfOrderSequenceException
import org.apache.kafka.common.errors.ProducerFencedException
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import java.util.concurrent.ExecutionException

fun main() {

    // ****************************************************************************************************************
    // Configure required properties for our Producer.
    // For details about all producer config properties, see http://kafka.apache.org/documentation.html#producerconfigs.
    // ****************************************************************************************************************
    val props = Properties()

    // You must connect to one of the brokers in the cluster (any broker is fine).
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"

    // If you want to send messages to a broker, you must run in a transaction. So specify a transaction id.
    props[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = "my-transactional-id"

    // You must tell Kafka how to serialize keys (in our example, keys will be Integers).
    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = IntegerSerializer::class.java.name

    // You must also tell Kafka how to serialize values (in our example, values will be Strings).
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name


    // ****************************************************************************************************************
    // Configure optional properties for our Producer.

    // It's a good idea to define an id for our producer, in case we get problems and need to fish around in log files.
    props[ProducerConfig.CLIENT_ID_CONFIG] = "producer-coroutines-topic"

    // When we publish a record (to the leader broker), wait until all followers have acknowledged receipt, then we consider the record has been sent successfully.
    props[ProducerConfig.ACKS_CONFIG] = "all"

    // Tell producer to attempt to batch records together into fewer requests (up to 5k batch size) to improve throughput.
    props[ProducerConfig.BATCH_SIZE_CONFIG] = 5000

    // Tell producer to deliberately wait 1/4sec between sends, to increase the chances of batching (even under moderate load).
    props[ProducerConfig.LINGER_MS_CONFIG] = 250

    // Specify 1s as the max time we'll wait for a send to complete, otherwise timeout.
    props[ProducerConfig.MAX_BLOCK_MS_CONFIG] = 2000

    // ****************************************************************************************************************
    // Create our Producer - keys are Integers, values are Strings.
    val producer: Producer<Int, String> = KafkaProducer(props)

    // Wrap all message-sends in a transaction, which we commit or rollback atomically.
    producer.initTransactions()

    // Specify topic name. Note, our Kanfa config allows for topics to be auto-created.
    val topic = "coroutines-input"

    // Begin a transaction, to wrap all message sends.
    producer.beginTransaction()
    try {
        for (i in 100..199) {

            // Create a record (key is Integer, value is String).
            Faker.instance().chuckNorris().fact()
            val v = Faker.instance().chuckNorris().fact()
            val record = ProducerRecord(topic, i, v)

            // Send message asynchronously.
            val f = producer.send(record)

            try {
                // Get metadata about the sent record. This is a blocking operation.
                val m = f.get()
                System.out.printf(
                    "[%s,%s] sent to topic %s, partition %s, offset %d\n",
                    i, v, m.topic(), m.partition(), m.offset()
                )
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }


            Thread.sleep(1000)
            println("sleeping for 1 second..")
        }
        // If we get this far, all records were sent successfully, so commit the transaction.
        producer.commitTransaction()

    } catch (e: ProducerFencedException) {
        // We can't recover from these exceptions, so our only option is to close the producer and exit.
        producer.close()

    } catch (e: OutOfOrderSequenceException) {
        producer.close()

    } catch (e: AuthorizationException) {
        producer.close()

    } catch (e: KafkaException) {
        // For all other exceptions, just abort the transaction and try again.
        producer.abortTransaction()
    }

    // Remember to close the producer at the end, to avoid resource leakage (e.g. threads).
    producer.close()
}


