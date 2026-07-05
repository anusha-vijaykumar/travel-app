package com.springcloud.notification_service.kafka;

import com.springcloud.notification_service.dto.PaymentResultEvent;
import com.springcloud.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentResultConsumerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final PaymentResultConsumer paymentResultConsumer = new PaymentResultConsumer(notificationService);

    @Test
    void consumeDelegatesToNotificationService() {
        PaymentResultEvent event = new PaymentResultEvent(1L, "SUCCESS", "CONFIRMED");

        paymentResultConsumer.consume(event);

        verify(notificationService).sendPaymentCompletedEmail(event);
    }
}
