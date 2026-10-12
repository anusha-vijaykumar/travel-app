package com.springcloud.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ApiGatewayApplicationTests {

    @Test
    void applicationClassHasExpectedSpringAnnotations() {
        assertThat(ApiGatewayApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(ApiGatewayApplication.class).hasAnnotation(EnableDiscoveryClient.class);
    }

    @Test
    void gatewayRoutesUseDiscoveryServiceIds() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yaml"));

        Properties properties = yaml.getObject();
        assertThat(properties).isNotNull();

        Map<String, String> routeUrisById = IntStream.range(0, 4)
                .boxed()
                .collect(Collectors.toMap(
                        index -> properties.getProperty("spring.cloud.gateway.routes[" + index + "].id"),
                        index -> properties.getProperty("spring.cloud.gateway.routes[" + index + "].uri")
                ));

        assertThat(routeUrisById)
                .containsEntry("user-service", "lb://user-service")
                .containsEntry("inventory-service", "lb://inventory-service")
                .containsEntry("booking-service", "lb://booking-service")
                .containsEntry("payment-service", "lb://payment-service");
    }
}
