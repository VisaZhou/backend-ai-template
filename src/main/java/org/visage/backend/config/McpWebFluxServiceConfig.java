package org.visage.backend.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.visage.backend.tool.DateTimeTool;

@Configuration
public class McpWebFluxServiceConfig {

    @Bean
    public ToolCallbackProvider dateTools(DateTimeTool dateTimeTool) {
        return MethodToolCallbackProvider.builder().toolObjects(dateTimeTool).build();
    }
}
