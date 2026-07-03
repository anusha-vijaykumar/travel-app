package com.springcloud.booking_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aggregateType;
    private Long aggregateId;
    private String eventType;
    private String topic;
    @Lob
    @Column(nullable = false)
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    private Integer attempts;
    @Column(length = 1000)
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
