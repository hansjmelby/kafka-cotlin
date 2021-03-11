package com.example.demo


import com.github.javafaker.Faker
import com.github.javafaker.Weather
import example.avro.Sensor
import org.apache.kafka.streams.kstream.ForeachAction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink

import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.Bean
import org.springframework.kafka.support.converter.*


import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import org.apache.kafka.streams.kstream.KStream
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.generic.GenericRecord

import org.apache.kafka.common.serialization.Serde
import org.springframework.cloud.stream.schema.client.EnableSchemaRegistryClient


@SpringBootApplication
@EnableSchemaRegistryClient
class DemoApplication {
	private val random = Random()


	@Bean
	fun produceBook(): Supplier<Message<Book>> {
		return Supplier {
			var book = Faker.instance().book()
			Faker.instance().commerce().price(50.0,400.0)

			var weather = Faker.instance().weather()
			var builder = MessageBuilder.withPayload(Book(book.author(),book.title(),book.publisher(),book.genre(),Faker.instance().commerce().price(50.0,400.0).replace(",",".").toDouble()))
			builder.build()

		}
	}
	@Bean
	fun consumeBook(): Consumer<Message<Book>> {
		return Consumer {
			s:Message<Book>->
				println("Book sold : ${s.payload} ")
		}
	}

	@Bean
	fun produceSensor(): Supplier<Sensor>? {
		return Supplier<Sensor> {
			val sensor = Sensor()
			sensor.setId(UUID.randomUUID().toString().toString() + "-v1")
			sensor.setAcceleration(random.nextFloat() * 10)
			sensor.setVelocity(random.nextFloat() * 100)
			sensor.setTemperature(random.nextFloat() * 50)
			sensor
		}
	}

	@Bean
	fun consumeSensor(): Consumer<Message<Sensor>>{
		return Consumer {

			println("consumeSensor headers : ${it.headers}")
			println("consumeSensor : ${it.payload}")
		}

	}
	@Bean
	fun streamSensor(): Consumer<KStream<Sensor?, Sensor>>? {
		return Consumer { input: KStream<Sensor?, Sensor> ->
			input.peek { key: Sensor?, value: Sensor ->
				println(
					" streamSensor: $value"
				)
			}
		}

	}

	@Bean
	fun avroInSerde(): Serde<Sensor?>? {
		val avroInSerde = SpecificAvroSerde<Sensor>()
		val serdeProperties: Map<String, Any> = HashMap()
		return avroInSerde
	}

}
fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}


