package com.springcloud.inventory_service.service;

import com.springcloud.inventory_service.dto.InventoryDto;

public interface InventoryService {
    InventoryDto getInventoryById(Long id);
    InventoryDto getInventoryByTourPackageId(Long tourPackageId);
    InventoryDto createInventory(InventoryDto inventoryDto);
    void reserveSeats(Long tourPackageId, Integer seats);
    void releaseSeats(Long tourPackageId, Integer seats);
}


