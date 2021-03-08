package com.example.demo


import com.github.javafaker.Faker
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink

import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.Bean


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
		return Consumer {
			s:Message<String>->
				println("FACT : "+s.payload)
		}
	}


}
fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}


