package com.springcloud.payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dead_letter_payment_event")
public class DeadLetterPaymentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String topic;
    private Integer partitionId;
    private Long offsetValue;
    private String originalTopic;
    @Column(length = 1000)
    private String exceptionClassName;
    @Column(length = 1000)
    private String exceptionMessage;
    @Lob
    @Column(nullable = false)
    private String payload;
    private LocalDateTime createdAt;
}
