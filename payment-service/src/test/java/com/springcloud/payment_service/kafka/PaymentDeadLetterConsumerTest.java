package com.springcloud.payment_service.kafka;

import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.service.DeadLetterPaymentEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentDeadLetterConsumerTest {

    private final DeadLetterPaymentEventService deadLetterPaymentEventService = mock(DeadLetterPaymentEventService.class);
    private final PaymentDeadLetterConsumer paymentDeadLetterConsumer =
            new PaymentDeadLetterConsumer(deadLetterPaymentEventService);

    @Test
    void consumePersistsDeadLetterRecord() {
        ConsumerRecord<String, PaymentEvent> record = new ConsumerRecord<>(
                "payment-dead-letter-topic",
                0,
                1L,
                "booking-1",
                new PaymentEvent("test-event-id", 1L, BigDecimal.valueOf(100))
        );

        paymentDeadLetterConsumer.consume(record);

        verify(deadLetterPaymentEventService).save(record);
    }
}
