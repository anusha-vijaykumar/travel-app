package com.springcloud.notification_service.kafka;

import com.springcloud.notification_service.dto.PaymentResultEvent;
import com.springcloud.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentResultConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(topics = "${spring.kafka.payment-result-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PaymentResultEvent paymentResultEvent) {
        LOGGER.info("Payment result received for notification: {}", paymentResultEvent);
        notificationService.sendPaymentCompletedEmail(paymentResultEvent);
    }
}
