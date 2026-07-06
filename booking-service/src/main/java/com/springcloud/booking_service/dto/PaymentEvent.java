package com.springcloud.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    // A unique identifier for the event to enable idempotent consumers
    private String eventId;
    private Long bookingId;
    private BigDecimal amount;
}
