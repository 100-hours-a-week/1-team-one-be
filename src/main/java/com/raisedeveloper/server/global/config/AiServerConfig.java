package com.raisedeveloper.server.global.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiServerConfig {

	@Value("${ai.server.timeout}")
	private int timeout;

	@Bean
	public RestTemplate aiServerRestTemplate(RestTemplateBuilder builder) {
		return builder
			.connectTimeout(Duration.ofMillis(timeout))
			.readTimeout(Duration.ofMillis(timeout))
			.build();
	}
}
