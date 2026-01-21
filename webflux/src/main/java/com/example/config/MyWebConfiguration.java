package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class MyWebConfiguration {
    // 设置webflux的一些底层配置
    @Bean
    public WebFluxConfigurer webFluxConfigurer(){
        return new WebFluxConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                WebFluxConfigurer.super.addResourceHandlers(registry);
            }
        };
    }
}
