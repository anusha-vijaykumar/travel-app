package com.springcloud.booking_service.service;

import com.springcloud.booking_service.dto.BookingDto;
import com.springcloud.booking_service.entity.Booking;
import com.springcloud.booking_service.entity.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDto getBookingById(Long id);
    List<BookingDto> getBookingsByUserId(Long id);
    void cancelBookingById(Long id);
    void createBooking(BookingDto booking);
    Booking saveInitialBooking(BookingDto booking);
    void updateBookingStatus(Long id, BookingStatus bookingStatus);
    //BookingDto updateBooking(BookingDto booking);
}
