package com.springcloud.payment_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"booking_id"}) // Guarantees only one row per booking
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
