package com.springcloud.booking_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryDto(Long id, Long tourPackageId, String tourPackageName, LocalDate departureDate,
                           BigDecimal price, Integer totalCapacity, Integer availableSpots) {
}
