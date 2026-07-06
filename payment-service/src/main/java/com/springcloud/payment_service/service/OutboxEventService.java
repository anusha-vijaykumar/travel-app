package com.springcloud.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.payment_service.dto.PaymentResultEvent;
import com.springcloud.payment_service.entity.OutboxEvent;
import com.springcloud.payment_service.entity.OutboxStatus;
import com.springcloud.payment_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.payment-result-topic.name}")
    private String paymentResultTopic;

    public OutboxEvent savePaymentResultEvent(PaymentResultEvent paymentResultEvent) {
        // Ensure the event has an eventId so downstream consumers can de-duplicate
        if (paymentResultEvent.getEventId() == null) {
            paymentResultEvent.setEventId(java.util.UUID.randomUUID().toString());
        }
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("PAYMENT");
        outboxEvent.setAggregateId(paymentResultEvent.getBookingId());
        outboxEvent.setEventType("PAYMENT_COMPLETED");
        outboxEvent.setTopic(paymentResultTopic);
        outboxEvent.setPayload(toJson(paymentResultEvent));
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setAttempts(0);
        outboxEvent.setCreatedAt(LocalDateTime.now());
        return outboxEventRepository.save(outboxEvent);
    }

    private String toJson(PaymentResultEvent paymentResultEvent) {
        try {
            return objectMapper.writeValueAsString(paymentResultEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payment result event", e);
        }
    }
}
