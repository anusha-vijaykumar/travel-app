package com.springcloud.booking_service.client;

import com.springcloud.booking_service.dto.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", path = "/api/inventories")
public interface InventoryFeignClient {

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveSeats(@RequestParam Long tourPackageId, @RequestParam Integer seats);

    @GetMapping("/by-tour/{tourPackageId}")
    public ResponseEntity<InventoryDto> getInventoryByTourPackageId(@PathVariable Long tourPackageId);

    @PostMapping("/release")
    public ResponseEntity<Void> releaseSeats(@RequestParam Long tourPackageId, @RequestParam Integer seats);
}
