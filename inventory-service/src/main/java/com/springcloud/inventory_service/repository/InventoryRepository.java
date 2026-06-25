package com.springcloud.inventory_service.repository;

import com.springcloud.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Inventory getInventoryById(Long id);
    Inventory getInventoryByTourPackageId(Long tourPackageId);
}
