package com.springcloud.mcp_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "travel.api")
public record TravelApiProperties(String baseUrl) {
}
