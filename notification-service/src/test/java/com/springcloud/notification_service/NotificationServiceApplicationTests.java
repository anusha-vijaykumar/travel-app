package com.springcloud.notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(NotificationServiceApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(NotificationServiceApplication.class).hasAnnotation(EnableDiscoveryClient.class);
        assertThat(NotificationServiceApplication.class).hasAnnotation(EnableFeignClients.class);
        assertThat(NotificationServiceApplication.class).hasAnnotation(EnableKafka.class);
    }
}
