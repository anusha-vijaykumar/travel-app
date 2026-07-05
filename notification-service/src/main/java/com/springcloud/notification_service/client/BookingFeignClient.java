package com.springcloud.notification_service.client;

import com.springcloud.notification_service.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service", path = "/api/bookings")
public interface BookingFeignClient {

    @GetMapping("/{id}")
    BookingDto getBookingById(@PathVariable("id") Long id);
}
