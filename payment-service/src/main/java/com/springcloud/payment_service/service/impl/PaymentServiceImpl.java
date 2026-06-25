package com.springcloud.payment_service.service.impl;

import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.entity.Payment;
import com.springcloud.payment_service.entity.PaymentStatus;
import com.springcloud.payment_service.repository.PaymentRepository;
import com.springcloud.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private Random random =  new Random();

    @Override
    @Transactional // Added transactional support for database consistency
    public void createPayment(PaymentDto paymentDto) {
        // 70% success rate, 30% failure rate
        int chance = random.nextInt(100);

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
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment processing failed. Please try again.");
        }
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
