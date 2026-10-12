package com.springcloud.mcp_server.dto;

import java.math.BigDecimal;

public record BookingRequest(
        Long userId,
        Long tourId,
        Integer seatsBooked,
        BigDecimal amount,
        String idempotencyKey
) {
}
