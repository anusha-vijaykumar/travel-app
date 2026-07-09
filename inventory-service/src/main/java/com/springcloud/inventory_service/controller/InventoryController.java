package com.springcloud.inventory_service.controller;

import com.springcloud.inventory_service.dto.InventoryDto;
import com.springcloud.inventory_service.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventories")
@AllArgsConstructor
public class InventoryController {
    private InventoryService inventoryService;

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto> getInventoryById(@PathVariable Long id){
        InventoryDto inventoryDto = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventoryDto);
    }

    @GetMapping("/by-tour/{tourPackageId}")
    public ResponseEntity<InventoryDto> getInventoryByTourPackageId(@PathVariable Long tourPackageId){
        InventoryDto inventoryDto = inventoryService.getInventoryByTourPackageId(tourPackageId);
        return ResponseEntity.ok(inventoryDto);
    }

    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(@RequestBody InventoryDto inventoryDto){
        InventoryDto createdInventory = inventoryService.createInventory(inventoryDto);
        return ResponseEntity.ok(createdInventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(@PathVariable Long id, @RequestBody InventoryDto inventoryDto){
        InventoryDto updatedInventory = inventoryService.updateInventory(id, inventoryDto);
        return ResponseEntity.ok(updatedInventory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id){
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveSeats(@RequestParam Long tourPackageId, @RequestParam Integer seats) {
        inventoryService.reserveSeats(tourPackageId, seats);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseSeats(@RequestParam Long tourPackageId, @RequestParam Integer seats) {
        inventoryService.releaseSeats(tourPackageId, seats);
        return ResponseEntity.ok().build();
    }
}
