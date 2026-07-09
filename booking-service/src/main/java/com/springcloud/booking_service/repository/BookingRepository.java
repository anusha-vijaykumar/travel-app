package com.springcloud.booking_service.repository;

import com.springcloud.booking_service.entity.Booking;
import com.springcloud.booking_service.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    Booking  getBookingById(Long id);
    List<Booking> getBookingsByUserId(Long id);
    Booking getByIdempotencyKey(String idempotencyKey);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = :status AND b.createdAt < :cutoffTime")
    List<Booking> findExpiredBookings(@Param("status") BookingStatus status, @Param("cutoffTime") Timestamp cutoffTime);
}
