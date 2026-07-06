package com.springcloud.payment_service.kafka;

import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.entity.PaymentStatus;
import com.springcloud.payment_service.repository.ProcessedEventRepository;
import com.springcloud.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentConsumerTest {

    private final PaymentService paymentService = mock(PaymentService.class);
    private final ProcessedEventRepository processedEventRepository = mock(ProcessedEventRepository.class);
    private final PaymentConsumer paymentConsumer = new PaymentConsumer(paymentService, processedEventRepository);

    @Test
    void consumeCreatesPaymentForPaymentEvent() {
        doAnswer(invocation -> {
            PaymentDto paymentDto = invocation.getArgument(0);
            paymentDto.setStatus(PaymentStatus.SUCCESS);
            return null;
        }).when(paymentService).createPayment(any(PaymentDto.class));

        paymentConsumer.consume(new PaymentEvent("test-event-id", 1L, BigDecimal.valueOf(100)));

        ArgumentCaptor<PaymentDto> captor = ArgumentCaptor.forClass(PaymentDto.class);
        verify(paymentService).createPayment(captor.capture());
        assertThat(captor.getValue().getBookingId()).isEqualTo(1L);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void consumeCreatesPaymentWhenPaymentFails() {
        doAnswer(invocation -> {
            PaymentDto paymentDto = invocation.getArgument(0);
            paymentDto.setStatus(PaymentStatus.FAILED);
            return null;
        }).when(paymentService).createPayment(any(PaymentDto.class));

        paymentConsumer.consume(new PaymentEvent("test-event-id", 1L, BigDecimal.valueOf(100)));

        verify(paymentService).createPayment(any(PaymentDto.class));
    }
}
