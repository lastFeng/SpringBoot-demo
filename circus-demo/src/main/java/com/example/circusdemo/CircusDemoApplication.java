package com.example.circusdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@EnableCircuitBreaker
@SpringBootApplication
public class CircusDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CircusDemoApplication.class, args);
	}

}
