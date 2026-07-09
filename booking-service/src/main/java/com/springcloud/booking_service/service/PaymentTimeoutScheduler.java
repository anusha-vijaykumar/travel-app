package com.springcloud.booking_service.service;

import com.springcloud.booking_service.client.InventoryFeignClient;
import com.springcloud.booking_service.entity.Booking;
import com.springcloud.booking_service.entity.BookingStatus;
import com.springcloud.booking_service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

/**
 * Scheduled service to handle payment timeouts.
 * Releases reserved seats for bookings that haven't been confirmed within the timeout period.
 */
@Service
public class PaymentTimeoutScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTimeoutScheduler.class);

    private final BookingRepository bookingRepository;
    private final InventoryFeignClient inventoryFeignClient;

    @Value("${app.booking.payment-timeout-minutes:15}")
    private int paymentTimeoutMinutes;

    public PaymentTimeoutScheduler(BookingRepository bookingRepository,
                                   InventoryFeignClient inventoryFeignClient) {
        this.bookingRepository = bookingRepository;
        this.inventoryFeignClient = inventoryFeignClient;
    }

    /**
     * Scheduled task to check for expired payment requests and release reserved seats.
     * Runs every 2 minutes to balance between responsiveness and database load.
     */
    @Scheduled(fixedDelay = 120000) // 2 minutes in milliseconds
    @Transactional
    public void handlePaymentTimeouts() {
        try {
            // Calculate the cutoff time (current time - timeout minutes)
            long cutoffTimeMillis = System.currentTimeMillis() - (paymentTimeoutMinutes * 60 * 1000L);
            Timestamp cutoffTime = new Timestamp(cutoffTimeMillis);

            // Find all bookings stuck in PAYMENT_REQUESTED status beyond the timeout
            List<Booking> expiredBookings = bookingRepository.findExpiredBookings(BookingStatus.PAYMENT_REQUESTED, cutoffTime);

            if (expiredBookings.isEmpty()) {
                logger.debug("No expired bookings found");
                return;
            }

            logger.info("Found {} expired bookings. Processing payment timeouts.", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                try {
                    processExpiredBooking(booking);
                } catch (Exception e) {
                    logger.error("Error processing expired booking with id: {}", booking.getId(), e);
                    // Continue processing other bookings even if one fails
                }
            }

        } catch (Exception e) {
            logger.error("Error in payment timeout scheduler", e);
        }
    }

    /**
     * Process an individual expired booking by releasing reserved seats and updating status.
     */
    private void processExpiredBooking(Booking booking) {
        logger.info("Processing expired booking: id={}, tourId={}, seatsBooked={}",
                    booking.getId(), booking.getTourId(), booking.getSeatsBooked());

        // Release the reserved seats
        try {
            inventoryFeignClient.releaseSeats(booking.getTourId(), booking.getSeatsBooked());
            logger.info("Successfully released {} seats for tour {}", booking.getSeatsBooked(), booking.getTourId());
        } catch (Exception e) {
            logger.error("Failed to release seats for expired booking id: {}", booking.getId(), e);
            // Continue anyway to mark the booking as failed
        }

        // Update booking status to PAYMENT_FAILED
        booking.setBookingStatus(BookingStatus.PAYMENT_FAILED);
        bookingRepository.save(booking);
        logger.info("Updated booking {} status to PAYMENT_FAILED", booking.getId());
    }
}

