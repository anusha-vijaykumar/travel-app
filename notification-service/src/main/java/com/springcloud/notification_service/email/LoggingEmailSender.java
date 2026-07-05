package com.springcloud.notification_service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "notification.email.mode", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        LOGGER.info("Email notification queued to={}, subject={}, body={}", to, subject, body);
    }
}
