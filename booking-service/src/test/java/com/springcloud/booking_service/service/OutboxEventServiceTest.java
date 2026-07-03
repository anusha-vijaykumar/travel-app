package com.springcloud.booking_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.booking_service.dto.PaymentEvent;
import com.springcloud.booking_service.entity.OutboxEvent;
import com.springcloud.booking_service.entity.OutboxStatus;
import com.springcloud.booking_service.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

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
    void savePaymentRequestedEventStoresPendingOutboxEvent() {
        ReflectionTestUtils.setField(outboxEventService, "paymentRequestTopic", "booking-topic");
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OutboxEvent outboxEvent = outboxEventService.savePaymentRequestedEvent(
                new PaymentEvent(1L, BigDecimal.valueOf(100))
        );

        assertThat(outboxEvent.getAggregateType()).isEqualTo("BOOKING");
        assertThat(outboxEvent.getAggregateId()).isEqualTo(1L);
        assertThat(outboxEvent.getEventType()).isEqualTo("PAYMENT_REQUESTED");
        assertThat(outboxEvent.getTopic()).isEqualTo("booking-topic");
        assertThat(outboxEvent.getPayload()).contains("\"bookingId\":1");
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvent.getAttempts()).isZero();
        assertThat(outboxEvent.getCreatedAt()).isNotNull();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }
}
