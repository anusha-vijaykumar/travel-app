package com.springcloud.inventory_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tourPackageId;
    private String tourPackageName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-DD")
    private LocalDate departureDate;
    private BigDecimal price;
    private Integer totalCapacity;
    private Integer availableSpots;
}
