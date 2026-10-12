package com.springcloud.mcp_server.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String firstname,
        String lastname,
        String email,
        Instant createdAt
) {
}
