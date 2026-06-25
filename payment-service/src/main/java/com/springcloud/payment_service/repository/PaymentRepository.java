package com.springcloud.payment_service.repository;

import com.springcloud.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Payment getPaymentById(Long paymentId);
    Payment getPaymentByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.bookingId = :bookingId ORDER BY p.createdAt DESC LIMIT 1")
    Payment getLatestPaymentByBookingId(@Param("bookingId") Long bookingId);
}
