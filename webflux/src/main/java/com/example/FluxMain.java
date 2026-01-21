package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

// @EnableWebFlux // 开启webflux自定义配置，最好不要开启，因为开启后会禁用webflux的所有默认配置
@SpringBootApplication
public class FluxMain {
    public static void main(String[] args) {
        SpringApplication.run(FluxMain.class, args);
    }
}
