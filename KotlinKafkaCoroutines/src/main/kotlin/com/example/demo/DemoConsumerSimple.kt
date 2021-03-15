package com.example.demo

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.PartitionInfo
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*
import java.util.function.Consumer

class Runner {

    fun startConsumation() {

        // Configure required properties for our Consumer.
        // For details about all consumer config properties, see http://kafka.apache.org/documentation.html#consumerconfigs.
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ConsumerConfig.GROUP_ID_CONFIG] = "coroutines1"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = IntegerDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"


        // Create our Consumer - keys are Ints, values are Strings.
        val consumer: org.apache.kafka.clients.consumer.Consumer<Int, String> = KafkaConsumer(props)

        // Tell consumer to subscribe to topic.
        val topic = "coroutines-input"
        consumer.subscribe(listOf(topic))
        consumer.seekToBeginning(consumer.assignment())

        var noMessagesCount = 0
        val maxNoMessagesCount = 10
        while (true) {

            // Wait specified time for record(s) to arrive.
            val pollTimeout = Duration.ofSeconds(2)
            val records = consumer.poll(pollTimeout)


            // If no records received in specified time...
            if (records.count() == 0) {
                println("No messages...")
                if (++noMessagesCount > maxNoMessagesCount) {
                    break
                } else {
                    continue
                }
            }

            // Record(s) received, print them.
            System.out.printf("Poll returned %d record(s)\n", records.count())
            records.forEach(Consumer { r: ConsumerRecord<Int?, String?> ->
                System.out.printf(
                    "\t[%s,%s] received from topic %s, partition %s, offset %d\n",
                    r.key(), r.value(), r.topic(), r.partition(), r.offset()
                )
            })

            // Commit offsets returned on the last poll(), for the subscribed-to topics/partition.
            consumer.commitAsync()
        }

        // Remember to close the consumer at the end, to avoid resource leakage (e.g. TCP connections).
        consumer.close()
    }
}