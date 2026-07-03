package com.springcloud.booking_service.service;

import com.springcloud.booking_service.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto getBookingById(Long id);
    List<BookingDto> getBookingsByUserId(Long id);
    void cancelBookingById(Long id);
    void updateBookingStatusById(Long id, String status);
    void createBooking(BookingDto booking);
    //BookingDto updateBooking(BookingDto booking);
}
