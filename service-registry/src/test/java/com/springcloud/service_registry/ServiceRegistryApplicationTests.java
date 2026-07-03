package com.springcloud.service_registry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceRegistryApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(ServiceRegistryApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(ServiceRegistryApplication.class).hasAnnotation(EnableEurekaServer.class);
    }
}
