package com.springcloud.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import static org.assertj.core.api.Assertions.assertThat;

class ApiGatewayApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(ApiGatewayApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(ApiGatewayApplication.class).hasAnnotation(EnableDiscoveryClient.class);
    }
}
