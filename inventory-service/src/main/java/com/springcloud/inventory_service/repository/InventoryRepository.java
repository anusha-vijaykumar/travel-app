package com.springcloud.inventory_service.repository;

import com.springcloud.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Inventory getInventoryById(Long id);
    Inventory getInventoryByTourPackageId(Long tourPackageId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Inventory inventory
            set inventory.availableSpots = inventory.availableSpots - :seats
            where inventory.tourPackageId = :tourPackageId
              and inventory.availableSpots >= :seats
            """)
    int reserveSeatsIfAvailable(@Param("tourPackageId") Long tourPackageId, @Param("seats") Integer seats);
}
