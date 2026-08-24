package com.project.config;

import com.project.service.llm.GeminiLlmProvider;
import com.project.service.llm.LlmProvider;
import com.project.service.llm.MockLlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    @Value("${application.llm.provider:mock}")
    private String provider;

    @Value("${application.llm.api-url:https://generativelanguage.googleapis.com/v1beta}")
    private String apiUrl;

    @Value("${application.llm.api-key:}")
    private String apiKey;

    @Value("${application.llm.model:gemini-1.5-flash}")
    private String model;

    @Bean
    public RestTemplate llmRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Bean
    public LlmProvider llmProvider(RestTemplate llmRestTemplate) {
        if ("gemini".equalsIgnoreCase(provider) && apiKey != null && !apiKey.trim().isEmpty()) {
            log.info("Initializing GeminiLlmProvider with model: {}", model);
            return new GeminiLlmProvider(llmRestTemplate, apiUrl, apiKey, model);
        } else {
            log.info("Initializing MockLlmProvider (provider set to '{}' or API key is missing)", provider);
            return new MockLlmProvider();
        }
    }
}
