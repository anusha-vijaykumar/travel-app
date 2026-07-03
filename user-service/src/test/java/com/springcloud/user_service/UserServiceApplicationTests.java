package com.springcloud.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(UserServiceApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(UserServiceApplication.class).hasAnnotation(EnableDiscoveryClient.class);
    }
}
