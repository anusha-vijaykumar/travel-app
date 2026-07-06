package com.springcloud.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultEvent {
    // Event id provided by the producer (Payment Service) to enable idempotency checks
    private String eventId;
    private Long bookingId;
    private String paymentStatus;
    private String bookingStatus;
}
