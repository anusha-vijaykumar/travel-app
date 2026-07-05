package com.springcloud.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Long id;
    private Long userId;
    private Long tourId;
    private Integer seatsBooked;
    private BigDecimal amount;
    private String bookingStatus;
    private Timestamp createdAt;
    private String idempotencyKey;
}
