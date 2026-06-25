package com.springcloud.booking_service.service.impl;

import com.springcloud.booking_service.client.InventoryFeignClient;
import com.springcloud.booking_service.client.PaymentFeignClient;
import com.springcloud.booking_service.dto.BookingDto;
import com.springcloud.booking_service.dto.InventoryDto;
import com.springcloud.booking_service.entity.Booking;
import com.springcloud.booking_service.entity.BookingStatus;
import com.springcloud.booking_service.repository.BookingRepository;
import com.springcloud.booking_service.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.sql.Timestamp;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;
import com.springcloud.booking_service.dto.PaymentDto;
import com.springcloud.booking_service.dto.PaymentStatus;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    @Autowired
    @Lazy // Prevents a circular dependency crash at application startup
    private BookingService selfProxy;
    private BookingRepository bookingRepository;
    private InventoryFeignClient inventoryFeignClient;
    private PaymentFeignClient paymentFeignClient;

    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.getById(id);
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
        Booking booking = bookingRepository.getById(id);
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    public void createBooking(BookingDto booking) {

        // 1. Handle Idempotency check & initial insertion in a short, isolated transaction
        Booking savedBooking;
        try {
            savedBooking = selfProxy.saveInitialBooking(booking);
        } catch (DataIntegrityViolationException e) {
            // Race condition: another thread inserted the key at the exact same millisecond
            handleDuplicateRequest(booking.getIdempotencyKey());
            return;
        }

        // If the booking already existed and was handled, exit early
        if (savedBooking == null) return;

        try {
            // 2. Fetch inventory metadata (Read-Only network call)
            ResponseEntity<InventoryDto> inventoryResponse = inventoryFeignClient.getInventoryByTourPackageId(booking.getTourId());
            if (inventoryResponse.getStatusCode() != HttpStatus.OK || inventoryResponse.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found for tourId " + booking.getTourId());
            }

            InventoryDto inventory = inventoryResponse.getBody();
            java.math.BigDecimal calculatedAmount = inventory.price().multiply(java.math.BigDecimal.valueOf(booking.getSeatsBooked()));

            // Validate amounts
            if (booking.getAmount() == null || booking.getAmount().compareTo(calculatedAmount) != 0) {
                selfProxy.updateBookingStatus(savedBooking.getId(), BookingStatus.CANCELLED);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount mismatch.");
            }

            // 3. Reserve seats (Network Mutation call)
            ResponseEntity<Void> response = inventoryFeignClient.reserveSeats(booking.getTourId(), booking.getSeatsBooked());
            if (response.getStatusCode() != HttpStatus.OK) {
                selfProxy.updateBookingStatus(savedBooking.getId(), BookingStatus.CANCELLED);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to reserve seats.");
            }

            // 4. Process Payment (The Point of No Return)
            // Pass the unique savedBooking.getId() as Stripe/PayPal's idempotency/reference key!
            PaymentDto paymentDto = new PaymentDto(null, savedBooking.getId(), booking.getAmount(), null, null);

            try {
                ResponseEntity<PaymentDto> paymentResponse = paymentFeignClient.processPayment(paymentDto);

                if (paymentResponse.getStatusCode() == HttpStatus.OK && paymentResponse.getBody() != null) {
                    PaymentDto processedPayment = paymentResponse.getBody();

                    if (processedPayment.status() == PaymentStatus.SUCCESS &&
                            processedPayment.amount().compareTo(booking.getAmount()) == 0) {

                        // Success! Update local status in an isolated transaction
                        selfProxy.updateBookingStatus(savedBooking.getId(), BookingStatus.CONFIRMED);
                    } else {
                        handleFailureCleanup(savedBooking, booking);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment failed or amount mismatch.");
                    }
                } else {
                    handleFailureCleanup(savedBooking, booking);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment service error.");
                }
            } catch (FeignException e) {
                handleFailureCleanup(savedBooking, booking);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment gateway unreachable.");
            }

        } catch (FeignException e) {
            selfProxy.updateBookingStatus(savedBooking.getId(), BookingStatus.CANCELLED);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Inventory service error.");
        }
    }

    @Transactional
    public Booking saveInitialBooking(BookingDto booking) {
        // Check if key already exists
        Booking existingBooking = bookingRepository.getByIdempotencyKey(booking.getIdempotencyKey());
        if (existingBooking != null) {
            handleDuplicateRequest(booking.getIdempotencyKey());
            return null; // Signals to orchestrator that it's already handled
        }

        // Save the row immediately with the idempotency key to claim it
        Booking newBooking = new Booking();
        newBooking.setUserId(booking.getUserId());
        newBooking.setTourId(booking.getTourId());
        newBooking.setSeatsBooked(booking.getSeatsBooked());
        newBooking.setIdempotencyKey(booking.getIdempotencyKey()); // Don't forget to save this!
        newBooking.setBookingStatus(BookingStatus.PENDING);
        newBooking.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        return bookingRepository.save(newBooking);
    }

    @Transactional
    public void updateBookingStatus(Long bookingId, BookingStatus status) {
        bookingRepository.findById(bookingId).ifPresent(b -> {
            b.setBookingStatus(status);
            bookingRepository.save(b);
        });
    }

    // Helper method for network cleanup
    private void handleFailureCleanup(Booking savedBooking, BookingDto booking) {
        selfProxy.updateBookingStatus(savedBooking.getId(), BookingStatus.CANCELLED);
        try {
            // Release seats asynchronously or via fallback network call
            inventoryFeignClient.releaseSeats(booking.getTourId(), booking.getSeatsBooked());
        } catch (Exception e) {
            // Log critical error for manual reconciliation or background retry worker
        }
    }

    private void handleDuplicateRequest(String key) {
        Booking existing = bookingRepository.getByIdempotencyKey(key);
        if (existing.getBookingStatus() == BookingStatus.CONFIRMED) {
            return; // Industry Standard: Return silently (200 OK) because work is already done!
        } else {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Your booking is currently processing.");
        }
    }

}
