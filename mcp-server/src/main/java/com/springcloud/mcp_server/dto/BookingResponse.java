package com.springcloud.mcp_server.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
        Long id,
        Long userId,
        Long tourId,
        Integer seatsBooked,
        BigDecimal amount,
        String bookingStatus,
        Instant createdAt,
        String idempotencyKey
) {
}
