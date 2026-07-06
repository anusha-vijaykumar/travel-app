package com.springcloud.payment_service.service.impl;

import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.dto.PaymentResultEvent;
import com.springcloud.payment_service.entity.Payment;
import com.springcloud.payment_service.entity.PaymentStatus;
import com.springcloud.payment_service.exception.PaymentTransientException;
import com.springcloud.payment_service.repository.PaymentRepository;
import com.springcloud.payment_service.service.OutboxEventService;
import com.springcloud.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OutboxEventService outboxEventService;
    private final Random random;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, OutboxEventService outboxEventService) {
        this(paymentRepository, outboxEventService, new Random());
    }

    public PaymentServiceImpl(PaymentRepository paymentRepository, OutboxEventService outboxEventService, Random random) {
        this.paymentRepository = paymentRepository;
        this.outboxEventService = outboxEventService;
        this.random = random;
    }

    @Override
    @Transactional // Added transactional support for database consistency
    public void createPayment(PaymentDto paymentDto) {
        // 70% success, 20% business failure, 10% transient failure.
        int chance = random.nextInt(100);

        if (chance >= 90) {
            throw new PaymentTransientException("Transient payment processor failure for bookingId=" + paymentDto.getBookingId());
        }

        // FIX: Look up an existing record for this booking, or build a new one if missing
        Optional<Payment> existingPayment = Optional.ofNullable(paymentRepository.getPaymentByBookingId(paymentDto.getBookingId()));
        Payment payment = existingPayment.orElseGet(Payment::new);

        // Set properties (id should not be set manually if it is auto-incrementing)
        payment.setAmount(paymentDto.getAmount());
        payment.setBookingId(paymentDto.getBookingId());

        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);

        if (chance < 70) {
            // Payment successful
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment); // Spring converts this to a SQL UPDATE if it already existed

            paymentDto.setStatus(PaymentStatus.SUCCESS);
            paymentDto.setCreatedAt(now);
        } else {
            // Payment failed
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment); // Updates the row status to FAILED in-place

            paymentDto.setStatus(PaymentStatus.FAILED);
            paymentDto.setCreatedAt(now);
        }

        outboxEventService.savePaymentResultEvent(toPaymentResultEvent(paymentDto));
    }

    private PaymentResultEvent toPaymentResultEvent(PaymentDto paymentDto) {
        String bookingStatus = paymentDto.getStatus() == PaymentStatus.SUCCESS ? "CONFIRMED" : "PAYMENT_FAILED";
        // Generate an eventId for the payment result so consumers can perform idempotency checks
        String eventId = java.util.UUID.randomUUID().toString();
        return new PaymentResultEvent(
                eventId,
                paymentDto.getBookingId(),
                paymentDto.getStatus().name(),
                bookingStatus
        );
    }


    @Override
    public void refundPaymentForBookingId(Long bookId) {
        Payment payment = paymentRepository.getPaymentByBookingId(bookId);
        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found or not successful for booking ID: " + bookId);
        }
    }

    @Override
    public PaymentDto getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.getPaymentByBookingId(bookingId);
        if (payment != null) {
            return new PaymentDto(payment.getId(), payment.getBookingId(), payment.getAmount(), payment.getStatus(), payment.getCreatedAt());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for booking ID: " + bookingId);
        }
    }
}
