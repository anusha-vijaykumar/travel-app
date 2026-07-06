package com.springcloud.payment_service.kafka;

import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.entity.ProcessedEvent;
import com.springcloud.payment_service.repository.ProcessedEventRepository;
import com.springcloud.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);
    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(PaymentEvent paymentEvent) {
        LOGGER.info("Payment event received: {}", paymentEvent);

        String eventId = paymentEvent.getEventId();
        if (eventId != null && processedEventRepository.existsById(eventId)) {
            LOGGER.info("Duplicate PaymentEvent ignored: eventId={}", eventId);
            return;
        }

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setBookingId(paymentEvent.getBookingId());
        paymentDto.setAmount(paymentEvent.getAmount());
        paymentService.createPayment(paymentDto);

        if (eventId != null) {
            processedEventRepository.save(new ProcessedEvent(eventId, LocalDateTime.now()));
        }
    }
}
