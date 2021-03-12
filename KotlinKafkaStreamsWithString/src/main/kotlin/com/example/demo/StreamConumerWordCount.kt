package com.example.demo


import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import java.util.*
import java.util.concurrent.CountDownLatch

fun main() {

	val props = Properties()
	props[StreamsConfig.APPLICATION_ID_CONFIG] = "streams-wordcount4"
	props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
	props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
	props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass

	val builder = StreamsBuilder()
	val source = builder.stream<String, String>("streams-plaintext-input")

	/*
	source.foreach { key, value ->
		println("=======$key=========")
		System.out.println(value)
	}

	 */
	source.flatMapValues { value: String -> value.toLowerCase().split("\\W+".toRegex()) }
		.map { _: String?, value: String -> KeyValue(value, value) }
		.groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
		.count(Materialized.`as`("counts-store"))
		.toStream()
		.to("streams-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()))

	val topology = builder.build()
	val streams = KafkaStreams(topology, props)
	val latch = CountDownLatch(1)

	// Attach shutdown handler to catch control-c
	Runtime.getRuntime().addShutdownHook(object : Thread("streams-shutdown-hook") {
		override fun run() {
			streams.close()
			latch.countDown()
		}
	})
	try {
		println("Calling start()")
		streams.start()
		latch.await()
	} catch (e: Throwable) {
		System.exit(1)
	}
	System.exit(0)
}
