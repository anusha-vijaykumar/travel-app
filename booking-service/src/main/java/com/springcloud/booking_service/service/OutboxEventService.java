package com.springcloud.booking_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.booking_service.dto.PaymentEvent;
import com.springcloud.booking_service.entity.OutboxEvent;
import com.springcloud.booking_service.entity.OutboxStatus;
import com.springcloud.booking_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.name}")
    private String paymentRequestTopic;

    public OutboxEvent savePaymentRequestedEvent(PaymentEvent paymentEvent) {
        // Ensure the event has an eventId so consumers can perform idempotency checks
        if (paymentEvent.getEventId() == null) {
            paymentEvent.setEventId(java.util.UUID.randomUUID().toString());
        }
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("BOOKING");
        outboxEvent.setAggregateId(paymentEvent.getBookingId());
        outboxEvent.setEventType("PAYMENT_REQUESTED");
        outboxEvent.setTopic(paymentRequestTopic);
        outboxEvent.setPayload(toJson(paymentEvent));
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setAttempts(0);
        outboxEvent.setCreatedAt(LocalDateTime.now());
        return outboxEventRepository.save(outboxEvent);
    }

    private String toJson(PaymentEvent paymentEvent) {
        try {
            return objectMapper.writeValueAsString(paymentEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payment event", e);
        }
    }
}
