package com.example.demo.simpleclient.lateevents

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

// $ kafka-topics --zookeeper localhost:2181 --create --topic events --replication-factor 1 --partitions 4

fun main(args: Array<String>) {
    SimpleProducer("localhost:9092").produce()
}

class SimpleProducer(brokers: String) {

    private val logger = Logger.getLogger("SimpleProducer")
    private val producer = createProducer(brokers)

    private fun createProducer(brokers: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    fun produce() {
        val now = System.currentTimeMillis()
        val delay = 1200L - Math.floorMod(now, 1000)
        val timer = Timer()

        timer.schedule(object : TimerTask() {
            override fun run() {
                val ts = System.currentTimeMillis()
                val second:Long = Math.floorMod(ts / 1000, 60).toLong()

                if (!second.equals(58L)) {
                    sendMessage(second, ts, "on time")
                }
                if (second.equals(2L)) {
                    sendMessage(58, ts - 4000, "late")
                }
            }
        }, delay, 1000L)
    }

    private fun sendMessage(id: Long, ts: Long, info: String) {
        val window = (ts / 10000) * 10000
        val value = "$window,$id,$info"
        val futureResult = producer.send(ProducerRecord("events", null, ts, "$id", value))
        logger.log(Level.INFO,"Sent a record: $value")
        futureResult.get()
    }
}