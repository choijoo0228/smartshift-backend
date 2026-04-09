package com.smartshift.smartshift_backend.config;

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
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173", // local dev
                                "http://smartshift-frontend-s3.s3-website-us-east-1.amazonaws.com",
                                "http://www.choijoo.dev",
                                "https://www.choijoo.dev"
                        )
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}