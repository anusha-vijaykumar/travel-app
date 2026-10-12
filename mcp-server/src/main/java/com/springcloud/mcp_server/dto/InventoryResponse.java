package com.springcloud.mcp_server.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryResponse(
        Long id,
        Long tourPackageId,
        String tourPackageName,
        LocalDate departureDate,
        BigDecimal price,
        Integer totalCapacity,
        Integer availableSpots
) {
}
