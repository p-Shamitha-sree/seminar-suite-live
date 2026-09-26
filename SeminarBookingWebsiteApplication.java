package com.example.seminar_booking_website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.seminar_booking_website")
@EntityScan("com.example.seminar_booking_website.model")
@EnableJpaRepositories("com.example.seminar_booking_website.repository")
public class SeminarBookingWebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeminarBookingWebsiteApplication.class, args);
	}
}
