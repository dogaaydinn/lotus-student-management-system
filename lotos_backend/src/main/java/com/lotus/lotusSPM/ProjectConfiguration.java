package com.lotus.lotusSPM;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ProjectConfiguration {

	@Autowired
	private ProjectProperties properties;

	@PostConstruct
	public void init() {
		log.info("Project configuration initialized. Display stocks: {}", properties.isDisplayStocks());
	}
}
