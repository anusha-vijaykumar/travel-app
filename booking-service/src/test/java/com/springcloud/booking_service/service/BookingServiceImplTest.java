package com.springcloud.booking_service.service;

import com.springcloud.booking_service.client.InventoryFeignClient;
import com.springcloud.booking_service.client.PaymentFeignClient;
import com.springcloud.booking_service.dto.BookingDto;
import com.springcloud.booking_service.dto.InventoryDto;
import com.springcloud.booking_service.dto.PaymentEvent;
import com.springcloud.booking_service.entity.Booking;
import com.springcloud.booking_service.entity.BookingStatus;
import com.springcloud.booking_service.repository.BookingRepository;
import com.springcloud.booking_service.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final InventoryFeignClient inventoryFeignClient = mock(InventoryFeignClient.class);
    private final PaymentFeignClient paymentFeignClient = mock(PaymentFeignClient.class);
    private final OutboxEventService outboxEventService = mock(OutboxEventService.class);
    private final PlatformTransactionManager transactionManager = mockTransactionManager();
    private final BookingServiceImpl bookingService = new BookingServiceImpl(
            bookingRepository,
            inventoryFeignClient,
            paymentFeignClient,
            outboxEventService,
            transactionManager
    );

    @Test
    void getBookingByIdMapsEntityToDto() {
        Booking booking = booking(1L, BookingStatus.PENDING);
        when(bookingRepository.getReferenceById(1L)).thenReturn(booking);

        BookingDto dto = bookingService.getBookingById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBookingStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(dto.getIdempotencyKey()).isEqualTo("key-1");
    }

    @Test
    void getBookingsByUserIdMapsAllBookings() {
        when(bookingRepository.getBookingsByUserId(5L)).thenReturn(List.of(
                booking(1L, BookingStatus.PENDING),
                booking(2L, BookingStatus.CONFIRMED)
        ));

        List<BookingDto> bookings = bookingService.getBookingsByUserId(5L);

        assertThat(bookings).hasSize(2);
        assertThat(bookings).extracting(BookingDto::getBookingStatus)
                .containsExactly(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    }

    @Test
    void cancelBookingMarksBookingCancelled() {
        Booking booking = booking(1L, BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBookingById(1L);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelConfirmedBookingReleasesSeatsAndRefundsPayment() {
        Booking booking = booking(1L, BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(inventoryFeignClient.releaseSeats(10L, 2)).thenReturn(ResponseEntity.ok().build());
        when(paymentFeignClient.refundPayment(1L)).thenReturn(ResponseEntity.ok("refunded"));

        bookingService.cancelBookingById(1L);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(inventoryFeignClient).releaseSeats(10L, 2);
        verify(paymentFeignClient).refundPayment(1L);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelPaymentRequestedBookingOnlyReleasesSeats() {
        Booking booking = booking(1L, BookingStatus.PAYMENT_REQUESTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(inventoryFeignClient.releaseSeats(10L, 2)).thenReturn(ResponseEntity.ok().build());

        bookingService.cancelBookingById(1L);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(inventoryFeignClient).releaseSeats(10L, 2);
        verify(paymentFeignClient, never()).refundPayment(any());
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelAlreadyCancelledBookingDoesNothing() {
        Booking booking = booking(1L, BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBookingById(1L);

        verify(inventoryFeignClient, never()).releaseSeats(any(), any());
        verify(paymentFeignClient, never()).refundPayment(any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelConfirmedBookingDoesNotSaveWhenInventoryReleaseFails() {
        Booking booking = booking(1L, BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(inventoryFeignClient.releaseSeats(10L, 2)).thenReturn(ResponseEntity.internalServerError().build());

        assertThatThrownBy(() -> bookingService.cancelBookingById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(paymentFeignClient, never()).refundPayment(any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBookingStatusByIdReleasesSeatsWhenPaymentFails() {
        Booking booking = booking(1L, BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(inventoryFeignClient.releaseSeats(10L, 2)).thenReturn(ResponseEntity.ok().build());

        bookingService.updateBookingStatusById(1L, "PAYMENT_FAILED");

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.PAYMENT_FAILED);
        verify(bookingRepository).save(booking);
        verify(inventoryFeignClient).releaseSeats(10L, 2);
    }

    @Test
    void updateBookingStatusByIdDoesNotReleaseSeatsWhenPaymentSucceeds() {
        Booking booking = booking(1L, BookingStatus.PAYMENT_REQUESTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.updateBookingStatusById(1L, "CONFIRMED");

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
        verify(inventoryFeignClient, never()).releaseSeats(any(), any());
    }

    @Test
    void updateBookingStatusByIdDoesNotReleaseSeatsWhenAlreadyPaymentFailed() {
        Booking booking = booking(1L, BookingStatus.PAYMENT_FAILED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.updateBookingStatusById(1L, "PAYMENT_FAILED");

        verify(bookingRepository).save(booking);
        verify(inventoryFeignClient, never()).releaseSeats(any(), any());
    }

    @Test
    void createBookingReservesSeatsAndSavesPaymentOutboxEvent() {
        BookingDto request = bookingDto(BigDecimal.valueOf(200));
        when(bookingRepository.getByIdempotencyKey("idem-1")).thenReturn(null);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setId(42L);
            return savedBooking;
        });
        when(inventoryFeignClient.getInventoryByTourPackageId(10L))
                .thenReturn(ResponseEntity.ok(new InventoryDto(
                        1L,
                        10L,
                        "Paris",
                        LocalDate.of(2026, 8, 1),
                        BigDecimal.valueOf(100),
                        10,
                        8
                )));
        when(inventoryFeignClient.reserveSeats(10L, 2)).thenReturn(ResponseEntity.ok().build());

        bookingService.createBooking(request);

        verify(inventoryFeignClient).reserveSeats(10L, 2);
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(outboxEventService).savePaymentRequestedEvent(captor.capture());
        assertThat(captor.getValue().getBookingId()).isEqualTo(42L);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    void createBookingDoesNotPersistBookingWhenAmountDoesNotMatchInventoryPrice() {
        BookingDto request = bookingDto(BigDecimal.valueOf(201));
        when(bookingRepository.getByIdempotencyKey("idem-1")).thenReturn(null);
        when(inventoryFeignClient.getInventoryByTourPackageId(10L))
                .thenReturn(ResponseEntity.ok(new InventoryDto(
                        1L,
                        10L,
                        "Paris",
                        LocalDate.of(2026, 8, 1),
                        BigDecimal.valueOf(100),
                        10,
                        8
                )));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(outboxEventService, never()).savePaymentRequestedEvent(any(PaymentEvent.class));
    }

    private static PlatformTransactionManager mockTransactionManager() {
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
        return transactionManager;
    }

    private static Booking booking(Long id, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setUserId(5L);
        booking.setTourId(10L);
        booking.setSeatsBooked(2);
        booking.setBookingStatus(status);
        booking.setCreatedAt(new Timestamp(1L));
        booking.setIdempotencyKey("key-" + id);
        return booking;
    }

    private static BookingDto bookingDto(BigDecimal amount) {
        return new BookingDto(null, 5L, 10L, 2, amount, null, null, "idem-1");
    }
}
