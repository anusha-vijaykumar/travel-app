package com.springcloud.payment_service.dto;


import com.springcloud.payment_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
