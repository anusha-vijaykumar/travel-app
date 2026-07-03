package com.springcloud.inventory_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryServiceApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(InventoryServiceApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(InventoryServiceApplication.class).hasAnnotation(EnableDiscoveryClient.class);
    }
}
