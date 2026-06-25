package com.springcloud.booking_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDto(Long id, Long bookingId, BigDecimal amount, PaymentStatus status, LocalDateTime createdAt){

}
