package com.springcloud.booking_service.kafka;

import com.springcloud.booking_service.dto.PaymentResultEvent;
import com.springcloud.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentResultConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final BookingService bookingService;

    @KafkaListener(
            topics = "${spring.kafka.payment-result-topic.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentResultKafkaListenerContainerFactory")
    public void consume(PaymentResultEvent paymentResultEvent) {
        LOGGER.info("Payment result received: {}", paymentResultEvent);
        bookingService.updateBookingStatusById(paymentResultEvent.getBookingId(), paymentResultEvent.getBookingStatus());
        LOGGER.info("Booking {} updated to {}", paymentResultEvent.getBookingId(), paymentResultEvent.getBookingStatus());
    }
}
