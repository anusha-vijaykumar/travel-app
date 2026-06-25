package com.springcloud.payment_service.controller;


import com.springcloud.payment_service.dto.PaymentDto;
import com.springcloud.payment_service.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDto> processPayment(@RequestBody PaymentDto paymentDto) {
        paymentService.createPayment(paymentDto);
        return ResponseEntity.ok(paymentDto);
    }

    @PutMapping("/{bookingId}/refund")
    public ResponseEntity<String> refundPayment(@PathVariable Long bookingId) {
        paymentService.refundPaymentForBookingId(bookingId);
        return ResponseEntity.ok("Payment refunded for booking ID: " + bookingId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<PaymentDto> getPaymentByBookingId(@PathVariable Long bookingId) {
        PaymentDto paymentDto = paymentService.getPaymentByBookingId(bookingId);
        if (paymentDto != null) {
            return ResponseEntity.ok(paymentDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
