package com.springcloud.booking_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class BookingServiceApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(BookingServiceApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(BookingServiceApplication.class).hasAnnotation(EnableDiscoveryClient.class);
        assertThat(BookingServiceApplication.class).hasAnnotation(EnableFeignClients.class);
        assertThat(BookingServiceApplication.class).hasAnnotation(EnableKafka.class);
        assertThat(BookingServiceApplication.class).hasAnnotation(EnableScheduling.class);
    }
}
