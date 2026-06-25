package com.springcloud.payment_service.service;

import com.springcloud.payment_service.dto.PaymentDto;

public interface PaymentService {
    void createPayment(PaymentDto payment);
    void refundPaymentForBookingId(Long bookId);
    PaymentDto getPaymentByBookingId(Long bookingId);
}
