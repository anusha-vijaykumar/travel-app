package com.springcloud.payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentServiceApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(PaymentServiceApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(PaymentServiceApplication.class).hasAnnotation(EnableDiscoveryClient.class);
        assertThat(PaymentServiceApplication.class).hasAnnotation(EnableKafka.class);
    }
}
