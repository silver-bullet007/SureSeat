package com.seatsure.seatsure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableKafka
public class SeatsureApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeatsureApplication.class, args);
	}

}
