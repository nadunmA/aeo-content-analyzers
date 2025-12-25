package com.aeo.analyzer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
public class AnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyzerApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())  // ← මේ line එක add කරන්න
				.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
				.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
				.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
				.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true)
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // optional: pretty date format
	}
}