package com.springcloud.booking_service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.booking_service.dto.PaymentEvent;
import com.springcloud.booking_service.entity.OutboxEvent;
import com.springcloud.booking_service.entity.OutboxStatus;
import com.springcloud.booking_service.kafka.BookingProducer;
import com.springcloud.booking_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final BookingProducer bookingProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${booking.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent outboxEvent : pendingEvents) {
            publish(outboxEvent);
        }
    }

    private void publish(OutboxEvent outboxEvent) {
        try {
            PaymentEvent paymentEvent = objectMapper.readValue(outboxEvent.getPayload(), PaymentEvent.class);
            bookingProducer.sendPaymentEvent(paymentEvent).get(10, TimeUnit.SECONDS);

            outboxEvent.setStatus(OutboxStatus.PUBLISHED);
            outboxEvent.setPublishedAt(LocalDateTime.now());
            outboxEvent.setLastError(null);
            outboxEventRepository.save(outboxEvent);
            LOGGER.info("Outbox event {} published for bookingId={}", outboxEvent.getId(), paymentEvent.getBookingId());
        } catch (Exception e) {
            outboxEvent.setAttempts(outboxEvent.getAttempts() == null ? 1 : outboxEvent.getAttempts() + 1);
            outboxEvent.setLastError(e.getMessage());
            outboxEventRepository.save(outboxEvent);
            LOGGER.error("Failed to publish outbox event {}", outboxEvent.getId(), e);
        }
    }
}
