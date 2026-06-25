package com.springcloud.inventory_service.service.impl;

import com.springcloud.inventory_service.dto.InventoryDto;
import com.springcloud.inventory_service.entity.Inventory;
import com.springcloud.inventory_service.repository.InventoryRepository;
import com.springcloud.inventory_service.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private InventoryRepository inventoryRepository;

    @Override
    public InventoryDto getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.getInventoryById(id);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for id " + id);
        }
        InventoryDto inventoryDto = new InventoryDto(inventory.getId(), inventory.getTourPackageId(), inventory.getTourPackageName(), inventory.getDepartureDate(), inventory.getPrice(), inventory.getTotalCapacity(), inventory.getAvailableSpots());
        return inventoryDto;
    }

    @Override
    public InventoryDto getInventoryByTourPackageId(Long tourPackageId) {
        Inventory inventory = inventoryRepository.getInventoryByTourPackageId(tourPackageId);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for tourPackageId " + tourPackageId);
        }
        InventoryDto inventoryDto = new InventoryDto(inventory.getId(), inventory.getTourPackageId(), inventory.getTourPackageName(), inventory.getDepartureDate(), inventory.getPrice(), inventory.getTotalCapacity(), inventory.getAvailableSpots());
        return inventoryDto;
    }

    @Override
    public InventoryDto createInventory(InventoryDto inventoryDto) {
        Inventory inventory = new Inventory(inventoryDto.getId(), inventoryDto.getTourPackageId(), inventoryDto.getTourPackageName(), inventoryDto.getDepartureDate(), inventoryDto.getPrice(), inventoryDto.getTotalCapacity(), inventoryDto.getAvailableSpots());
        Inventory savedInventory = inventoryRepository.save(inventory);
        InventoryDto savedInventoryDto = new InventoryDto(savedInventory.getId(), savedInventory.getTourPackageId(), savedInventory.getTourPackageName(), savedInventory.getDepartureDate(), savedInventory.getPrice(), savedInventory.getTotalCapacity(), savedInventory.getAvailableSpots());
        return savedInventoryDto;
    }

    @Override
    public void reserveSeats(Long tourPackageId, Integer seats) {
        Inventory inventory = inventoryRepository.getInventoryByTourPackageId(tourPackageId);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for tourPackageId " + tourPackageId);
        }
        if (inventory.getAvailableSpots() < seats) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough available spots for tourPackageId " + tourPackageId);
        }
        inventory.setAvailableSpots(inventory.getAvailableSpots() - seats);
        inventoryRepository.save(inventory);
    }

    @Override
    public void releaseSeats(Long tourPackageId, Integer seats) {
        Inventory inventory = inventoryRepository.getInventoryByTourPackageId(tourPackageId);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for tourPackageId " + tourPackageId);
        }
        inventory.setAvailableSpots(inventory.getAvailableSpots() + seats);
        inventoryRepository.save(inventory);
    }
}
