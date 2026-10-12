package com.springcloud.mcp_server.config;

import com.springcloud.mcp_server.tools.TravelTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    ToolCallbackProvider travelToolCallbackProvider(TravelTools travelTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(travelTools)
                .build();
    }
}
