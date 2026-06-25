package com.springcloud.booking_service.dto;

import com.springcloud.booking_service.entity.BookingStatus;
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
    private BigDecimal amount; // Amount the user is paying
    private BookingStatus bookingStatus;
    private Timestamp createdAt;
    private String idempotencyKey;
}
