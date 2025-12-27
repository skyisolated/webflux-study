package com.example.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rest")
public class HitokotoApi {
    private final WebClient webClient;

    public HitokotoApi() {
        this.webClient = WebClient.create("v1.hitokoto.cn");
    }
    @GetMapping("/say")
    public Mono<String> say() {
        Map<String, String> param = new HashMap<>();
        param.put("c", "a,c");
        Mono<String> body = webClient.get()
                .uri("", param)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
        return body;

    }
}
