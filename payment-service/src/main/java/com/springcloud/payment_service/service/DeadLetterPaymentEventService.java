package com.springcloud.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.entity.DeadLetterPaymentEvent;
import com.springcloud.payment_service.repository.DeadLetterPaymentEventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeadLetterPaymentEventService {
    private final DeadLetterPaymentEventRepository deadLetterPaymentEventRepository;
    private final ObjectMapper objectMapper;

    public DeadLetterPaymentEvent save(ConsumerRecord<String, PaymentEvent> record) {
        PaymentEvent paymentEvent = record.value();

        DeadLetterPaymentEvent deadLetterPaymentEvent = new DeadLetterPaymentEvent();
        deadLetterPaymentEvent.setBookingId(paymentEvent.getBookingId());
        deadLetterPaymentEvent.setAmount(paymentEvent.getAmount());
        deadLetterPaymentEvent.setTopic(record.topic());
        deadLetterPaymentEvent.setPartitionId(record.partition());
        deadLetterPaymentEvent.setOffsetValue(record.offset());
        deadLetterPaymentEvent.setOriginalTopic(headerValue(record, KafkaHeaders.DLT_ORIGINAL_TOPIC));
        deadLetterPaymentEvent.setExceptionClassName(headerValue(record, KafkaHeaders.DLT_EXCEPTION_FQCN));
        deadLetterPaymentEvent.setExceptionMessage(headerValue(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE));
        deadLetterPaymentEvent.setPayload(toJson(paymentEvent));
        deadLetterPaymentEvent.setCreatedAt(LocalDateTime.now());

        return deadLetterPaymentEventRepository.save(deadLetterPaymentEvent);
    }

    private String headerValue(ConsumerRecord<String, PaymentEvent> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    private String toJson(PaymentEvent paymentEvent) {
        try {
            return objectMapper.writeValueAsString(paymentEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize dead-letter payment event", e);
        }
    }
}
