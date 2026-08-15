package com.seatsure.seatsure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeatsureApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeatsureApplication.class, args);
	}

}
