package com.springcloud.mcp_server.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        Long bookingId,
        BigDecimal amount
) {
}
