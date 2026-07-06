package com.springcloud.booking_service.service.impl;

import com.springcloud.booking_service.client.InventoryFeignClient;
import com.springcloud.booking_service.dto.BookingDto;
import com.springcloud.booking_service.dto.InventoryDto;
import com.springcloud.booking_service.dto.PaymentEvent;
import com.springcloud.booking_service.entity.Booking;
import com.springcloud.booking_service.entity.BookingStatus;
import com.springcloud.booking_service.repository.BookingRepository;
import com.springcloud.booking_service.service.BookingService;
import com.springcloud.booking_service.service.OutboxEventService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.sql.Timestamp;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final InventoryFeignClient inventoryFeignClient;
    private final OutboxEventService outboxEventService;
    private final TransactionTemplate transactionTemplate;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              InventoryFeignClient inventoryFeignClient,
                              OutboxEventService outboxEventService,
                              PlatformTransactionManager transactionManager) {
        this.bookingRepository = bookingRepository;
        this.inventoryFeignClient = inventoryFeignClient;
        this.outboxEventService = outboxEventService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.getReferenceById(id);
        return new BookingDto(booking.getId(), booking.getUserId(), booking.getTourId(), booking.getSeatsBooked(),
                null, booking.getBookingStatus(), booking.getCreatedAt(), booking.getIdempotencyKey());
    }

    @Override
    public List<BookingDto> getBookingsByUserId(Long id) {
        List<Booking> bookings = bookingRepository.getBookingsByUserId(id);
        return bookings.stream().map(booking -> new BookingDto(booking.getId(), booking.getUserId(), booking.getTourId(), booking.getSeatsBooked(),
                null, booking.getBookingStatus(), booking.getCreatedAt(), booking.getIdempotencyKey())).toList();
    }

    @Override
    public void cancelBookingById(Long id) {
        Booking booking = bookingRepository.getReferenceById(id);
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    public void updateBookingStatusById(Long id, String status) {
        updateBookingStatusInTransaction(id, BookingStatus.valueOf(status));
    }

    @Override
    public void createBooking(BookingDto booking) {
        if (bookingRepository.getByIdempotencyKey(booking.getIdempotencyKey()) != null) {
            handleDuplicateRequest(booking.getIdempotencyKey());
            return;
        }

        boolean seatsReserved = false;
        try {
            // 1. Fetch inventory metadata.
            ResponseEntity<InventoryDto> inventoryResponse = inventoryFeignClient.getInventoryByTourPackageId(booking.getTourId());
            if (inventoryResponse.getStatusCode() != HttpStatus.OK || inventoryResponse.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found for tourId " + booking.getTourId());
            }

            InventoryDto inventory = inventoryResponse.getBody();
            java.math.BigDecimal calculatedAmount = inventory.price().multiply(java.math.BigDecimal.valueOf(booking.getSeatsBooked()));

            // Validate amounts
            if (booking.getAmount() == null || booking.getAmount().compareTo(calculatedAmount) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount mismatch.");
            }

            // 2. Reserve seats before persisting the booking.
            ResponseEntity<Void> response = inventoryFeignClient.reserveSeats(booking.getTourId(), booking.getSeatsBooked());
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to reserve seats.");
            }
            seatsReserved = true;

            // 3. Atomically persist the booking and outbox event.
            try {
                saveBookingAndOutboxEventInTransaction(booking);
            } catch (DataIntegrityViolationException e) {
                releaseReservedSeats(booking);
                handleDuplicateRequest(booking.getIdempotencyKey());
            } catch (ResponseStatusException e) {
                releaseReservedSeats(booking);
                throw e;
            } catch (Exception e) {
                releaseReservedSeats(booking);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save payment request.");
            }

        } catch (FeignException e) {
            if (seatsReserved) {
                releaseReservedSeats(booking);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Inventory service error.");
        }
    }

    private void saveBookingAndOutboxEventInTransaction(BookingDto booking) {
        transactionTemplate.executeWithoutResult(status -> {
            Booking savedBooking = saveBooking(booking);
            // Generate a UUID eventId for this PaymentEvent so downstream consumers can de-duplicate
            String eventId = java.util.UUID.randomUUID().toString();
            outboxEventService.savePaymentRequestedEvent(new PaymentEvent(eventId, savedBooking.getId(), booking.getAmount()));
        });
    }

    private Booking saveBooking(BookingDto booking) {
        Booking existingBooking = bookingRepository.getByIdempotencyKey(booking.getIdempotencyKey());
        if (existingBooking != null) {
            handleDuplicateRequest(booking.getIdempotencyKey());
        }

        Booking newBooking = new Booking();
        newBooking.setUserId(booking.getUserId());
        newBooking.setTourId(booking.getTourId());
        newBooking.setSeatsBooked(booking.getSeatsBooked());
        newBooking.setIdempotencyKey(booking.getIdempotencyKey());
        newBooking.setBookingStatus(BookingStatus.PAYMENT_REQUESTED);
        newBooking.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        return bookingRepository.save(newBooking);
    }

    private void updateBookingStatusInTransaction(Long bookingId, BookingStatus status) {
        transactionTemplate.executeWithoutResult(transactionStatus -> updateBookingStatus(bookingId, status));
    }

    private void updateBookingStatus(Long bookingId, BookingStatus status) {
        bookingRepository.findById(bookingId).ifPresent(b -> {
            b.setBookingStatus(status);
            bookingRepository.save(b);
        });
    }

    private void releaseReservedSeats(BookingDto booking) {
        try {
            inventoryFeignClient.releaseSeats(booking.getTourId(), booking.getSeatsBooked());
        } catch (Exception e) {
            // Log critical error for manual reconciliation or background retry worker
        }
    }

    private void handleDuplicateRequest(String key) {
        Booking existing = bookingRepository.getByIdempotencyKey(key);
        if (existing.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Your booking is currently processing.");
        }
    }

}
