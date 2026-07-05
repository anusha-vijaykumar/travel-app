package com.springcloud.notification_service.service;

import com.springcloud.notification_service.client.BookingFeignClient;
import com.springcloud.notification_service.client.UserFeignClient;
import com.springcloud.notification_service.dto.BookingDto;
import com.springcloud.notification_service.dto.PaymentResultEvent;
import com.springcloud.notification_service.dto.UserDto;
import com.springcloud.notification_service.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final BookingFeignClient bookingFeignClient;
    private final UserFeignClient userFeignClient;
    private final EmailSender emailSender;

    public void sendPaymentCompletedEmail(PaymentResultEvent paymentResultEvent) {
        BookingDto booking = bookingFeignClient.getBookingById(paymentResultEvent.getBookingId());
        UserDto user = userFeignClient.getUserById(booking.getUserId());

        String subject = "Travel booking payment " + paymentResultEvent.getPaymentStatus();
        String body = String.format(
                "Hello %s,%n%nYour payment for booking %d completed with status %s. Your booking status is %s.%n%nTravel App",
                user.getFirstname(),
                paymentResultEvent.getBookingId(),
                paymentResultEvent.getPaymentStatus(),
                paymentResultEvent.getBookingStatus()
        );

        emailSender.send(user.getEmail(), subject, body);
    }
}
