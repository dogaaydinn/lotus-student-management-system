package com.lotus.lotusSPM;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableConfigurationProperties(value = ProjectProperties.class)
public class LotusSpmApplication {

	public static void main(String[] args) {
		SpringApplication.run(LotusSpmApplication.class, args);
	}

	
}
