package com.springcloud.inventory_service.controller;

import com.springcloud.inventory_service.dto.InventoryDto;
import com.springcloud.inventory_service.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryControllerTest {

    private final InventoryService inventoryService = mock(InventoryService.class);
    private final InventoryController inventoryController = new InventoryController(inventoryService);

    @Test
    void getInventoryByIdReturnsDto() {
        InventoryDto dto = dto();
        when(inventoryService.getInventoryById(1L)).thenReturn(dto);

        ResponseEntity<InventoryDto> response = inventoryController.getInventoryById(1L);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void reserveSeatsDelegatesToService() {
        ResponseEntity<Void> response = inventoryController.reserveSeats(10L, 2);

        verify(inventoryService).reserveSeats(10L, 2);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void releaseSeatsDelegatesToService() {
        ResponseEntity<Void> response = inventoryController.releaseSeats(10L, 2);

        verify(inventoryService).releaseSeats(10L, 2);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private static InventoryDto dto() {
        return new InventoryDto(1L, 10L, "Paris", LocalDate.of(2026, 8, 1), BigDecimal.valueOf(100), 10, 5);
    }
}
