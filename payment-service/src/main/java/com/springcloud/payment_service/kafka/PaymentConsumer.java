package com.springcloud.payment_service.kafka;

import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConsumer.class);
    private final PaymentService paymentService;

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PaymentEvent paymentEvent) {
        LOGGER.info("Payment event received: {}", paymentEvent);

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setBookingId(paymentEvent.getBookingId());
        paymentDto.setAmount(paymentEvent.getAmount());
        paymentService.createPayment(paymentDto);
    }
}
