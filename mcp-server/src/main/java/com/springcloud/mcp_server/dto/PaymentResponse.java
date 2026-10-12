package com.springcloud.mcp_server.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long bookingId,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {
}
