package com.springcloud.payment_service.repository;

import com.springcloud.payment_service.entity.DeadLetterPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterPaymentEventRepository extends JpaRepository<DeadLetterPaymentEvent, Long> {
}
