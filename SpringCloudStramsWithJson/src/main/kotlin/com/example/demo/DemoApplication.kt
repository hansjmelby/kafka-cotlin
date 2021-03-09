package com.example.demo


import com.github.javafaker.Faker
import com.github.javafaker.Weather
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
import java.util.function.Consumer
import java.util.function.Supplier


@SpringBootApplication
class DemoApplication {

	@Bean
	fun produceWeather(): Supplier<Message<WeatherObject>> {
		return Supplier {
			var weather = Faker.instance().weather()
			var builder = MessageBuilder.withPayload(WeatherObject(weather.description(),weather.temperatureCelsius()))
			//builder.setHeader("content-type","application/json")
			builder.build()

		}
	}
	@Bean
	fun consumeWeather(): Consumer<Message<WeatherObject>> {
		return Consumer {
			s:Message<WeatherObject>->
				println("Weather is : ${s.payload.description} with the degree of ${s.payload.temperatureCelsius}")
		}
	}

	@Bean
	fun converter(): RecordMessageConverter {
		return StringJsonMessageConverter()
	}

}
fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}


