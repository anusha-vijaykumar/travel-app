package com.springcloud.booking_service.kafka;

import com.springcloud.booking_service.dto.PaymentEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class BookingProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingProducer.class);
    private final NewTopic topic;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public BookingProducer(NewTopic topic, KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, PaymentEvent>> sendPaymentEvent(PaymentEvent paymentEvent) {
        LOGGER.info("Sending payment event: {}", paymentEvent.toString());
        Message<PaymentEvent> message = MessageBuilder
                .withPayload(paymentEvent)
                .setHeader(KafkaHeaders.TOPIC, topic.name())
                .build();
        CompletableFuture<SendResult<String, PaymentEvent>> future = kafkaTemplate.send(message);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.error("Failed to send payment event for bookingId={}", paymentEvent.getBookingId(), ex);
                return;
            }

            LOGGER.info(
                    "Payment event sent to topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
        return future;
    }
}
