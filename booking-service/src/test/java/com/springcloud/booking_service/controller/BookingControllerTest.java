package com.springcloud.booking_service.controller;

import com.springcloud.booking_service.dto.BookingDto;
import com.springcloud.booking_service.entity.BookingStatus;
import com.springcloud.booking_service.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    private final BookingService bookingService = mock(BookingService.class);
    private final BookingController bookingController = new BookingController(bookingService);

    @Test
    void getBookingByIdReturnsBooking() {
        BookingDto bookingDto = bookingDto(1L);
        when(bookingService.getBookingById(1L)).thenReturn(bookingDto);

        ResponseEntity<BookingDto> response = bookingController.getBookingById(1L);

        assertThat(response.getBody()).isEqualTo(bookingDto);
    }

    @Test
    void getBookingsByUserIdReturnsBookings() {
        List<BookingDto> bookings = List.of(bookingDto(1L), bookingDto(2L));
        when(bookingService.getBookingsByUserId(5L)).thenReturn(bookings);

        ResponseEntity<List<BookingDto>> response = bookingController.getBookingsByUserId(5L);

        assertThat(response.getBody()).containsExactlyElementsOf(bookings);
    }

    @Test
    void createBookingDelegatesToService() {
        BookingDto bookingDto = bookingDto(1L);

        ResponseEntity<BookingDto> response = bookingController.createBooking(bookingDto);

        verify(bookingService).createBooking(bookingDto);
        assertThat(response.getBody()).isEqualTo(bookingDto);
    }

    @Test
    void cancelBookingDelegatesToService() {
        ResponseEntity<Long> response = bookingController.cancelBooking(1L);

        verify(bookingService).cancelBookingById(1L);
        assertThat(response.getBody()).isEqualTo(1L);
    }

    private static BookingDto bookingDto(Long id) {
        return new BookingDto(
                id,
                5L,
                10L,
                2,
                BigDecimal.valueOf(200),
                BookingStatus.PENDING,
                new Timestamp(1L),
                "idem-" + id
        );
    }
}
