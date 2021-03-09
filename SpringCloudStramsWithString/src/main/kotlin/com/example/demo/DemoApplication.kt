package com.example.demo


import com.github.javafaker.Faker
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.context.annotation.Bean
import java.util.function.Function

import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

import java.util.function.Consumer
import java.util.function.Supplier

@SpringBootApplication
class DemoApplication {

	@Bean
	fun produceChuckNoris(): Supplier<Message<String>> {
		return Supplier {
			MessageBuilder.withPayload(Faker.instance().chuckNorris().fact()).build()

		}
	}

	@Bean
	fun consumeChuckNoris(): Consumer<Message<String>> {
		return Consumer { s: Message<String> ->
			println("FACT : " + s.payload)
		}
	}

	@Bean
	fun processWords():Function<KStream<String?, String>, KStream<String, Long>> {
		return Function { inputStream: KStream<String?, String> ->
			val countStream = inputStream
				.flatMapValues { value: String -> value.toLowerCase().split("\\W+".toRegex()) }
				.map { _: String?, value: String -> KeyValue(value, value) }
				.groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
				.count(Materialized.`as`("word-count-state-store"))
				.toStream()
			countStream.to("counts", Produced.with(Serdes.String(), Serdes.Long()))
			countStream
		}
	}


}
fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}


