package com.springcloud.inventory_service.service;

import com.springcloud.inventory_service.dto.InventoryDto;
import com.springcloud.inventory_service.entity.Inventory;
import com.springcloud.inventory_service.repository.InventoryRepository;
import com.springcloud.inventory_service.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryServiceImplTest {

    private final InventoryRepository inventoryRepository = mock(InventoryRepository.class);
    private final InventoryServiceImpl inventoryService = new InventoryServiceImpl(inventoryRepository);

    @Test
    void getInventoryByTourPackageIdReturnsDto() {
        Inventory inventory = inventory();
        when(inventoryRepository.getInventoryByTourPackageId(10L)).thenReturn(inventory);

        InventoryDto dto = inventoryService.getInventoryByTourPackageId(10L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTourPackageId()).isEqualTo(10L);
        assertThat(dto.getAvailableSpots()).isEqualTo(5);
    }

    @Test
    void getInventoryByIdThrowsNotFoundWhenMissing() {
        when(inventoryRepository.getInventoryById(99L)).thenReturn(null);

        assertThatThrownBy(() -> inventoryService.getInventoryById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createInventorySavesAndReturnsCreatedDto() {
        Inventory inventory = inventory();
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDto saved = inventoryService.createInventory(toDto(inventory));

        assertThat(saved.getId()).isEqualTo(1L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void reserveSeatsDecrementsAvailableSpots() {
        Inventory inventory = inventory();
        when(inventoryRepository.getInventoryByTourPackageId(10L)).thenReturn(inventory);

        inventoryService.reserveSeats(10L, 2);

        assertThat(inventory.getAvailableSpots()).isEqualTo(3);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reserveSeatsThrowsWhenCapacityIsInsufficient() {
        Inventory inventory = inventory();
        when(inventoryRepository.getInventoryByTourPackageId(10L)).thenReturn(inventory);

        assertThatThrownBy(() -> inventoryService.reserveSeats(10L, 6))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void releaseSeatsIncrementsAvailableSpots() {
        Inventory inventory = inventory();
        when(inventoryRepository.getInventoryByTourPackageId(10L)).thenReturn(inventory);

        inventoryService.releaseSeats(10L, 2);

        assertThat(inventory.getAvailableSpots()).isEqualTo(7);
        verify(inventoryRepository).save(inventory);
    }

    private static Inventory inventory() {
        return new Inventory(1L, 10L, "Paris", LocalDate.of(2026, 8, 1), BigDecimal.valueOf(100), 10, 5);
    }

    private static InventoryDto toDto(Inventory inventory) {
        return new InventoryDto(
                inventory.getId(),
                inventory.getTourPackageId(),
                inventory.getTourPackageName(),
                inventory.getDepartureDate(),
                inventory.getPrice(),
                inventory.getTotalCapacity(),
                inventory.getAvailableSpots()
        );
    }
}
