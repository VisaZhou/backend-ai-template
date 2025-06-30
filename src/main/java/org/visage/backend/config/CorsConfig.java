package org.visage.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/**") // 所有接口
                        .allowedOriginPatterns("*") // 允许所有域
                        .allowedMethods("*") // 允许所有方法 GET POST PUT DELETE
                        .allowedHeaders("*") // 允许所有请求头
                        .allowCredentials(true) // 如果需要带cookie
                        .maxAge(3600);
            }
        };
    }
}