package com.springcloud.payment_service.service;

import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.dto.PaymentResultEvent;
import com.springcloud.payment_service.entity.Payment;
import com.springcloud.payment_service.entity.PaymentStatus;
import com.springcloud.payment_service.exception.PaymentTransientException;
import com.springcloud.payment_service.repository.PaymentRepository;
import com.springcloud.payment_service.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OutboxEventService outboxEventService = mock(OutboxEventService.class);
    private final Random random = mock(Random.class);
    private final PaymentServiceImpl paymentService =
            new PaymentServiceImpl(paymentRepository, outboxEventService, random);

    @Test
    void createPaymentSavesSuccessfulPaymentWhenChanceIsBelowThreshold() {
        when(random.nextInt(100)).thenReturn(69);
        when(paymentRepository.getPaymentByBookingId(1L)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PaymentDto paymentDto = new PaymentDto(null, 1L, BigDecimal.valueOf(100), null, null);

        paymentService.createPayment(paymentDto);

        assertThat(paymentDto.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(paymentDto.getCreatedAt()).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
        ArgumentCaptor<PaymentResultEvent> captor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(outboxEventService).savePaymentResultEvent(captor.capture());
        assertThat(captor.getValue().getBookingStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void createPaymentSavesBusinessFailedPaymentWhenChanceIsBetweenBusinessFailureThresholds() {
        when(random.nextInt(100)).thenReturn(89);
        when(paymentRepository.getPaymentByBookingId(1L)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PaymentDto paymentDto = new PaymentDto(null, 1L, BigDecimal.valueOf(100), null, null);

        paymentService.createPayment(paymentDto);

        assertThat(paymentDto.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(paymentDto.getCreatedAt()).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
        ArgumentCaptor<PaymentResultEvent> captor = ArgumentCaptor.forClass(PaymentResultEvent.class);
        verify(outboxEventService).savePaymentResultEvent(captor.capture());
        assertThat(captor.getValue().getBookingStatus()).isEqualTo("PAYMENT_FAILED");
    }

    @Test
    void createPaymentThrowsTransientExceptionWithoutSavingWhenChanceIsAtTransientThreshold() {
        when(random.nextInt(100)).thenReturn(90);
        PaymentDto paymentDto = new PaymentDto(null, 1L, BigDecimal.valueOf(100), null, null);

        assertThatThrownBy(() -> paymentService.createPayment(paymentDto))
                .isInstanceOf(PaymentTransientException.class)
                .hasMessageContaining("bookingId=1");

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(outboxEventService, never()).savePaymentResultEvent(any(PaymentResultEvent.class));
    }

    @Test
    void refundPaymentMarksSuccessfulPaymentRefunded() {
        Payment payment = new Payment(1L, 2L, BigDecimal.valueOf(100), PaymentStatus.SUCCESS, LocalDateTime.now());
        when(paymentRepository.getPaymentByBookingId(2L)).thenReturn(payment);

        paymentService.refundPaymentForBookingId(2L);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(payment);
    }

    @Test
    void refundPaymentThrowsWhenPaymentIsMissing() {
        when(paymentRepository.getPaymentByBookingId(2L)).thenReturn(null);

        assertThatThrownBy(() -> paymentService.refundPaymentForBookingId(2L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPaymentByBookingIdReturnsDto() {
        LocalDateTime createdAt = LocalDateTime.now();
        when(paymentRepository.getPaymentByBookingId(2L))
                .thenReturn(new Payment(1L, 2L, BigDecimal.valueOf(100), PaymentStatus.SUCCESS, createdAt));

        PaymentDto payment = paymentService.getPaymentByBookingId(2L);

        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getBookingId()).isEqualTo(2L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getCreatedAt()).isEqualTo(createdAt);
    }
}
