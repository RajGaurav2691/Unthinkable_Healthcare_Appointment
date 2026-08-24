package com.project.service.email;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockEmailService implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("================ MOCK EMAIL SENT ================");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body:\n{}", body);
        log.info("=================================================");
    }
}
