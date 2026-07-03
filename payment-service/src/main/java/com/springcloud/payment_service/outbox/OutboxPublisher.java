package com.springcloud.payment_service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.payment_service.dto.PaymentResultEvent;
import com.springcloud.payment_service.entity.OutboxEvent;
import com.springcloud.payment_service.entity.OutboxStatus;
import com.springcloud.payment_service.kafka.PaymentResultProducer;
import com.springcloud.payment_service.repository.OutboxEventRepository;
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
    private final PaymentResultProducer paymentResultProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${payment.outbox.publish-delay-ms:5000}")
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
            PaymentResultEvent paymentResultEvent =
                    objectMapper.readValue(outboxEvent.getPayload(), PaymentResultEvent.class);
            paymentResultProducer.sendPaymentResult(paymentResultEvent).get(10, TimeUnit.SECONDS);

            outboxEvent.setStatus(OutboxStatus.PUBLISHED);
            outboxEvent.setPublishedAt(LocalDateTime.now());
            outboxEvent.setLastError(null);
            outboxEventRepository.save(outboxEvent);
            LOGGER.info("Outbox event {} published for bookingId={}", outboxEvent.getId(), paymentResultEvent.getBookingId());
        } catch (Exception e) {
            outboxEvent.setAttempts(outboxEvent.getAttempts() == null ? 1 : outboxEvent.getAttempts() + 1);
            outboxEvent.setLastError(e.getMessage());
            outboxEventRepository.save(outboxEvent);
            LOGGER.error("Failed to publish outbox event {}", outboxEvent.getId(), e);
        }
    }
}
