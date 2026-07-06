package com.springcloud.payment_service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.payment_service.dto.PaymentResultEvent;
import com.springcloud.payment_service.entity.OutboxEvent;
import com.springcloud.payment_service.entity.OutboxStatus;
import com.springcloud.payment_service.kafka.PaymentResultProducer;
import com.springcloud.payment_service.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublisherTest {

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final PaymentResultProducer paymentResultProducer = mock(PaymentResultProducer.class);
    private final OutboxPublisher outboxPublisher =
            new OutboxPublisher(outboxEventRepository, paymentResultProducer, new ObjectMapper());

    @Test
    void publishPendingEventsMarksEventPublishedAfterKafkaAck() throws Exception {
        OutboxEvent outboxEvent = outboxEvent();
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(outboxEvent));
        when(paymentResultProducer.sendPaymentResult(any(PaymentResultEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        outboxPublisher.publishPendingEvents();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(outboxEvent.getPublishedAt()).isNotNull();
        verify(outboxEventRepository).save(outboxEvent);
    }

    @Test
    void publishPendingEventsKeepsEventPendingAndIncrementsAttemptsWhenKafkaFails() throws Exception {
        OutboxEvent outboxEvent = outboxEvent();
        CompletableFuture<SendResult<String, PaymentResultEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(outboxEvent));
        when(paymentResultProducer.sendPaymentResult(any(PaymentResultEvent.class))).thenReturn(failedFuture);

        outboxPublisher.publishPendingEvents();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getAttempts()).isEqualTo(1);
        assertThat(outboxEvent.getLastError()).isNotBlank();
        verify(outboxEventRepository).save(outboxEvent);
    }

    private static OutboxEvent outboxEvent() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(1L);
        outboxEvent.setAggregateType("PAYMENT");
        outboxEvent.setAggregateId(10L);
        outboxEvent.setEventType("PAYMENT_COMPLETED");
        outboxEvent.setTopic("payment-result-topic");
        outboxEvent.setPayload(objectMapper.writeValueAsString(
                new PaymentResultEvent("test-event-id", 10L, "SUCCESS", "CONFIRMED")
        ));
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setAttempts(0);
        outboxEvent.setCreatedAt(LocalDateTime.now());
        return outboxEvent;
    }
}
