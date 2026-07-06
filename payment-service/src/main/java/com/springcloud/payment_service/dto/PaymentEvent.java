package com.springcloud.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    // Event id injected by producer (Booking Service) for idempotency
    private String eventId;
    private Long bookingId;
    private BigDecimal amount;
}
