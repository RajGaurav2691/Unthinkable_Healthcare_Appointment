package com.project.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SendGridEmailService implements EmailService {

    private final String apiKey;
    private final String fromEmail;
    private final RestTemplate restTemplate;

    private static final String SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send";

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("SendGrid API key is not configured.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "personalizations", List.of(
                        Map.of("to", List.of(Map.of("email", to)))
                ),
                "from", Map.of("email", fromEmail),
                "subject", subject,
                "content", List.of(
                        Map.of("type", "text/plain", "value", body)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(SENDGRID_URL, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent email to {} via SendGrid", to);
            } else {
                throw new RuntimeException("Failed to send email via SendGrid: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", to, e.getMessage());
            throw new RuntimeException("SendGrid Error: " + e.getMessage(), e);
        }
    }
}
