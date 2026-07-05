package com.springcloud.payment_service.kafka;

import com.springcloud.payment_service.dto.PaymentEvent;
import com.springcloud.payment_service.service.DeadLetterPaymentEventService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentDeadLetterConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentDeadLetterConsumer.class);
    private final DeadLetterPaymentEventService deadLetterPaymentEventService;

    @KafkaListener(
            topics = "${spring.kafka.payment-dead-letter-topic.name}",
            groupId = "${spring.kafka.consumer.group-id}-dead-letter",
            containerFactory = "paymentDeadLetterKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, PaymentEvent> record) {
        deadLetterPaymentEventService.save(record);
        LOGGER.info(
                "Persisted dead-letter payment event for bookingId={} from topic={}, partition={}, offset={}",
                record.value().getBookingId(),
                record.topic(),
                record.partition(),
                record.offset()
        );
    }
}
