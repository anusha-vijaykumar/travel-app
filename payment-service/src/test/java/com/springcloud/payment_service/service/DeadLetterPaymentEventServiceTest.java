package com.springcloud.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.entity.DeadLetterPaymentEvent;
import com.springcloud.payment_service.repository.DeadLetterPaymentEventRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeadLetterPaymentEventServiceTest {

    private final DeadLetterPaymentEventRepository repository = mock(DeadLetterPaymentEventRepository.class);
    private final DeadLetterPaymentEventService service =
            new DeadLetterPaymentEventService(repository, new ObjectMapper());

    @Test
    void savePersistsDeadLetterPaymentEventWithMetadataAndPayload() {
        when(repository.save(any(DeadLetterPaymentEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ConsumerRecord<String, PaymentEvent> record = new ConsumerRecord<>(
                "payment-dead-letter-topic",
                2,
                15L,
                "booking-1",
                new PaymentEvent("test-event-id", 1L, BigDecimal.valueOf(100))
        );
        record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, "booking-topic".getBytes());
        record.headers().add(KafkaHeaders.DLT_EXCEPTION_FQCN, "com.springcloud.payment_service.exception.PaymentTransientException".getBytes());
        record.headers().add(KafkaHeaders.DLT_EXCEPTION_MESSAGE, "Transient payment processor failure".getBytes());

        DeadLetterPaymentEvent saved = service.save(record);

        assertThat(saved.getBookingId()).isEqualTo(1L);
        assertThat(saved.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(saved.getTopic()).isEqualTo("payment-dead-letter-topic");
        assertThat(saved.getPartitionId()).isEqualTo(2);
        assertThat(saved.getOffsetValue()).isEqualTo(15L);
        assertThat(saved.getOriginalTopic()).isEqualTo("booking-topic");
        assertThat(saved.getExceptionClassName()).contains("PaymentTransientException");
        assertThat(saved.getExceptionMessage()).contains("Transient payment processor failure");
        assertThat(saved.getPayload()).contains("\"bookingId\":1");
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(repository).save(any(DeadLetterPaymentEvent.class));
    }
}
