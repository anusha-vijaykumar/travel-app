package com.springcloud.booking_service.kafka;

import com.springcloud.booking_service.dto.PaymentResultEvent;
import com.springcloud.booking_service.repository.ProcessedEventRepository;
import com.springcloud.booking_service.service.BookingService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentResultConsumerTest {

    private final BookingService bookingService = mock(BookingService.class);
    private final ProcessedEventRepository processedEventRepository = mock(ProcessedEventRepository.class);
    private final PaymentResultConsumer paymentResultConsumer =
        new PaymentResultConsumer(bookingService, processedEventRepository);

    @Test
    void consumeUpdatesBookingStatusFromPaymentResult() {
        PaymentResultEvent event = new PaymentResultEvent("test-event-id", 1L, "FAILED", "PAYMENT_FAILED");

        paymentResultConsumer.consume(event);

        verify(bookingService).updateBookingStatusById(1L, "PAYMENT_FAILED");
    }
}
