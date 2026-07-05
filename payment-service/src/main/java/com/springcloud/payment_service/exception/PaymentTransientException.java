package com.springcloud.payment_service.exception;

public class PaymentTransientException extends RuntimeException {
    public PaymentTransientException(String message) {
        super(message);
    }
}
