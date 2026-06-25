package com.springcloud.booking_service.client;

import com.springcloud.booking_service.dto.PaymentDto;
import com.springcloud.booking_service.dto.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;

@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentFeignClient {
    @PostMapping
    @CircuitBreaker(name = "paymentCircuitBreaker", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentRetry")
    public ResponseEntity<PaymentDto> processPayment(@RequestBody PaymentDto paymentDto);

    default ResponseEntity<PaymentDto> paymentFallback(PaymentDto paymentDto, Throwable exception) {
        // Create a fake failed payment response to return to the orchestrator safely
        PaymentDto fallbackResponse = new PaymentDto(
                null,
                paymentDto.bookingId(),
                paymentDto.amount(),
                PaymentStatus.FAILED,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
    }
}
