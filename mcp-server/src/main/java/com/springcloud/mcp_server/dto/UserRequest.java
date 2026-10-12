package com.springcloud.mcp_server.dto;

public record UserRequest(
        String firstname,
        String lastname,
        String email
) {
}
