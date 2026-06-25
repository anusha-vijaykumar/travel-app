package com.springcloud.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDto {
    private Long id;
    private Long tourPackageId;
    private String tourPackageName;
    private LocalDate departureDate;
    private BigDecimal price;
    private Integer totalCapacity;
    private Integer availableSpots;
}
