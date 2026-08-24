package com.project.config;

import com.project.service.email.EmailService;
import com.project.service.email.MockEmailService;
import com.project.service.email.SendGridEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class EmailConfig {

    @Value("${application.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${application.email.sendgrid-api-key:}")
    private String sendGridApiKey;

    @Value("${application.email.from-email:noreply@example.com}")
    private String fromEmail;

    @Bean
    public EmailService emailService(RestTemplate restTemplate) {
        if (!emailEnabled || sendGridApiKey == null || sendGridApiKey.isBlank()) {
            log.info("Initializing MockEmailService (email.enabled is false or API key is missing)");
            return new MockEmailService();
        }
        log.info("Initializing SendGridEmailService with from-email: {}", fromEmail);
        return new SendGridEmailService(sendGridApiKey, fromEmail, restTemplate);
    }
}
