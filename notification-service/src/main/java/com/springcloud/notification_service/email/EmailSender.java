package com.springcloud.notification_service.email;

public interface EmailSender {
    void send(String to, String subject, String body);
}
