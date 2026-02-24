package com.ericross.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JobStoriesTrackerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobStoriesTrackerBackendApplication.class, args);
	}

}
