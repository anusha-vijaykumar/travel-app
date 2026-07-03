package com.springcloud.payment_service.controller;

import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.entity.PaymentStatus;
import com.springcloud.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    private final PaymentService paymentService = mock(PaymentService.class);
    private final PaymentController paymentController = new PaymentController(paymentService);

    @Test
    void processPaymentDelegatesToService() {
        PaymentDto paymentDto = new PaymentDto(null, 1L, BigDecimal.valueOf(100), null, null);

        ResponseEntity<PaymentDto> response = paymentController.processPayment(paymentDto);

        verify(paymentService).createPayment(paymentDto);
        assertThat(response.getBody()).isEqualTo(paymentDto);
    }

    @Test
    void refundPaymentDelegatesToService() {
        ResponseEntity<String> response = paymentController.refundPayment(1L);

        verify(paymentService).refundPaymentForBookingId(1L);
        assertThat(response.getBody()).contains("1");
    }

    @Test
    void getPaymentByBookingIdReturnsPayment() {
        PaymentDto paymentDto = new PaymentDto(1L, 2L, BigDecimal.valueOf(100), PaymentStatus.SUCCESS, LocalDateTime.now());
        when(paymentService.getPaymentByBookingId(2L)).thenReturn(paymentDto);

        ResponseEntity<PaymentDto> response = paymentController.getPaymentByBookingId(2L);

        assertThat(response.getBody()).isEqualTo(paymentDto);
    }
}
