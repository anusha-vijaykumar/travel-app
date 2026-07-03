package com.springcloud.payment_service.kafka;

import com.springcloud.payment_service.dto.PaymentResultEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PaymentResultProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResultProducer.class);

    private final NewTopic paymentResultTopic;
    private final KafkaTemplate<String, PaymentResultEvent> paymentResultKafkaTemplate;

    public PaymentResultProducer(@Qualifier("paymentResultTopic") NewTopic paymentResultTopic,
                                 KafkaTemplate<String, PaymentResultEvent> paymentResultKafkaTemplate) {
        this.paymentResultTopic = paymentResultTopic;
        this.paymentResultKafkaTemplate = paymentResultKafkaTemplate;
    }

    public CompletableFuture<SendResult<String, PaymentResultEvent>> sendPaymentResult(PaymentResultEvent paymentResultEvent) {
        Message<PaymentResultEvent> message = MessageBuilder
                .withPayload(paymentResultEvent)
                .setHeader(KafkaHeaders.TOPIC, paymentResultTopic.name())
                .build();

        CompletableFuture<SendResult<String, PaymentResultEvent>> future = paymentResultKafkaTemplate.send(message);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.error("Failed to send payment result for bookingId={}", paymentResultEvent.getBookingId(), ex);
                return;
            }

            LOGGER.info(
                    "Payment result sent to topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
        return future;
    }
}
