package com.springcloud.booking_service.repository;

import com.springcloud.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    Booking  getBookingById(Long id);
    List<Booking> getBookingsByUserId(Long id);
    Booking getByIdempotencyKey(String idempotencyKey);
}
