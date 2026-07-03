package com.springcloud.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.payment_service.dto.PaymentResultEvent;
import com.springcloud.payment_service.entity.OutboxEvent;
import com.springcloud.payment_service.entity.OutboxStatus;
import com.springcloud.payment_service.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxEventServiceTest {

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final OutboxEventService outboxEventService =
            new OutboxEventService(outboxEventRepository, new ObjectMapper());

    @Test
    void savePaymentResultEventStoresPendingOutboxEvent() {
        ReflectionTestUtils.setField(outboxEventService, "paymentResultTopic", "payment-result-topic");
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OutboxEvent outboxEvent = outboxEventService.savePaymentResultEvent(
                new PaymentResultEvent(1L, "SUCCESS", "CONFIRMED")
        );

        assertThat(outboxEvent.getAggregateType()).isEqualTo("PAYMENT");
        assertThat(outboxEvent.getAggregateId()).isEqualTo(1L);
        assertThat(outboxEvent.getEventType()).isEqualTo("PAYMENT_COMPLETED");
        assertThat(outboxEvent.getTopic()).isEqualTo("payment-result-topic");
        assertThat(outboxEvent.getPayload()).contains("\"bookingId\":1");
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getAttempts()).isZero();
        assertThat(outboxEvent.getCreatedAt()).isNotNull();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }
}
