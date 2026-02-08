package com.mateandgit.devstep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DevstepApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevstepApplication.class, args);
	}

}
