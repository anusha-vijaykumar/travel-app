package com.springcloud.inventory_service.service.impl;

import com.springcloud.inventory_service.dto.InventoryDto;
import com.springcloud.inventory_service.entity.Inventory;
import com.springcloud.inventory_service.repository.InventoryRepository;
import com.springcloud.inventory_service.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private InventoryRepository inventoryRepository;

    @Override
    @Cacheable(value = "inventory", key = "#id")
    public InventoryDto getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.getInventoryById(id);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for id " + id);
        }
        return new InventoryDto(inventory.getId(), inventory.getTourPackageId(), inventory.getTourPackageName(), inventory.getDepartureDate(), inventory.getPrice(), inventory.getTotalCapacity(), inventory.getAvailableSpots());
    }

    @Override
    @Cacheable(value = "inventory", key = "#tourPackageId")
    public InventoryDto getInventoryByTourPackageId(Long tourPackageId) {
        Inventory inventory = inventoryRepository.getInventoryByTourPackageId(tourPackageId);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for tourPackageId " + tourPackageId);
        }
        return new InventoryDto(inventory.getId(), inventory.getTourPackageId(), inventory.getTourPackageName(), inventory.getDepartureDate(), inventory.getPrice(), inventory.getTotalCapacity(), inventory.getAvailableSpots());
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    public InventoryDto createInventory(InventoryDto inventoryDto) {
        Inventory inventory = new Inventory(inventoryDto.getId(), inventoryDto.getTourPackageId(), inventoryDto.getTourPackageName(), inventoryDto.getDepartureDate(), inventoryDto.getPrice(), inventoryDto.getTotalCapacity(), inventoryDto.getAvailableSpots());
        Inventory savedInventory = inventoryRepository.save(inventory);
        return new InventoryDto(savedInventory.getId(), savedInventory.getTourPackageId(), savedInventory.getTourPackageName(), savedInventory.getDepartureDate(), savedInventory.getPrice(), savedInventory.getTotalCapacity(), savedInventory.getAvailableSpots());
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    public InventoryDto updateInventory(Long id, InventoryDto inventoryDto) {
        Inventory inventory = inventoryRepository.getInventoryById(id);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for id " + id);
        }
        inventory.setTourPackageId(inventoryDto.getTourPackageId());
        inventory.setTourPackageName(inventoryDto.getTourPackageName());
        inventory.setDepartureDate(inventoryDto.getDepartureDate());
        inventory.setPrice(inventoryDto.getPrice());
        inventory.setTotalCapacity(inventoryDto.getTotalCapacity());
        inventory.setAvailableSpots(inventoryDto.getAvailableSpots());
        Inventory updatedInventory = inventoryRepository.save(inventory);
        return new InventoryDto(updatedInventory.getId(), updatedInventory.getTourPackageId(), updatedInventory.getTourPackageName(), updatedInventory.getDepartureDate(), updatedInventory.getPrice(), updatedInventory.getTotalCapacity(), updatedInventory.getAvailableSpots());
    }

    @Override
    @CacheEvict(value = "inventory", allEntries = true)
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.getInventoryById(id);
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for id " + id);
        }
        inventoryRepository.deleteById(id);
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
