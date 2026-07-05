package com.springcloud.notification_service.service;

import com.springcloud.notification_service.client.BookingFeignClient;
import com.springcloud.notification_service.client.UserFeignClient;
import com.springcloud.notification_service.dto.BookingDto;
import com.springcloud.notification_service.dto.PaymentResultEvent;
import com.springcloud.notification_service.dto.UserDto;
import com.springcloud.notification_service.email.EmailSender;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private final BookingFeignClient bookingFeignClient = mock(BookingFeignClient.class);
    private final UserFeignClient userFeignClient = mock(UserFeignClient.class);
    private final EmailSender emailSender = mock(EmailSender.class);
    private final NotificationService notificationService =
            new NotificationService(bookingFeignClient, userFeignClient, emailSender);

    @Test
    void sendPaymentCompletedEmailLooksUpBookingAndUserThenSendsEmail() {
        PaymentResultEvent event = new PaymentResultEvent(10L, "SUCCESS", "CONFIRMED");
        when(bookingFeignClient.getBookingById(10L))
                .thenReturn(new BookingDto(10L, 7L, 20L, 2, BigDecimal.valueOf(200), "CONFIRMED", new Timestamp(1L), "idem-1"));
        when(userFeignClient.getUserById(7L))
                .thenReturn(new UserDto(7L, "Ada", "Lovelace", "ada@example.com", new Timestamp(1L)));

        notificationService.sendPaymentCompletedEmail(event);

        verify(emailSender).send(
                contains("ada@example.com"),
                contains("SUCCESS"),
                contains("booking 10")
        );
    }
}
