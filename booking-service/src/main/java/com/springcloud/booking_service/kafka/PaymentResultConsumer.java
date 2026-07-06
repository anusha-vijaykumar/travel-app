package com.springcloud.booking_service.kafka;

import com.springcloud.booking_service.dto.PaymentResultEvent;
import com.springcloud.booking_service.entity.ProcessedEvent;
import com.springcloud.booking_service.repository.ProcessedEventRepository;
import com.springcloud.booking_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentResultConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResultConsumer.class);

    private final BookingService bookingService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(
            topics = "${spring.kafka.payment-result-topic.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentResultKafkaListenerContainerFactory")
    @KafkaListener(
            topics = "${spring.kafka.payment-result-topic.name}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentResultKafkaListenerContainerFactory")
    @Transactional
    public void consume(PaymentResultEvent paymentResultEvent) {
        LOGGER.info("Payment result received: {}", paymentResultEvent);

        String eventId = paymentResultEvent.getEventId();
        if (eventId != null && processedEventRepository.existsById(eventId)) {
            LOGGER.info("Duplicate PaymentResultEvent ignored: eventId={}", eventId);
            return;
        }

        // Process business logic
        bookingService.updateBookingStatusById(paymentResultEvent.getBookingId(), paymentResultEvent.getBookingStatus());
        LOGGER.info("Booking {} updated to {}", paymentResultEvent.getBookingId(), paymentResultEvent.getBookingStatus());

        // Persist processed event marker so future duplicates are ignored
        if (eventId != null) {
            ProcessedEvent marker = new ProcessedEvent(eventId, LocalDateTime.now());
            processedEventRepository.save(marker);
        }
    }
}
