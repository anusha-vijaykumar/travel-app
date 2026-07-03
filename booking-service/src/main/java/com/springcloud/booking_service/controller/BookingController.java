package com.springcloud.booking_service.controller;


import com.springcloud.booking_service.dto.BookingDto;
import com.springcloud.booking_service.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@AllArgsConstructor
public class BookingController {
    private BookingService bookingService;

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable("id") Long id){
        BookingDto bookingDto = bookingService.getBookingById(id);
        return ResponseEntity.ok(bookingDto);
    }
    
    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookingsByUserId(@RequestParam("userId") Long id){
        List<BookingDto> bookingDto = bookingService.getBookingsByUserId(id);
        return ResponseEntity.ok(bookingDto);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestBody BookingDto bookingDto){
        bookingService.createBooking(bookingDto);
        return ResponseEntity.ok(bookingDto);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Long> cancelBooking(@PathVariable Long id){
        bookingService.cancelBookingById(id);
        return ResponseEntity.ok(id);
    }

}
