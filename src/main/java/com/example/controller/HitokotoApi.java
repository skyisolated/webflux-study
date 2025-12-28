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
        this.webClient = WebClient.create("https://v1.hitokoto.cn");
    }
    @GetMapping("/say")
    public Mono<String> say() {
        Mono<String> body = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("c", "a")
                        .queryParam("c", "c")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
        return body;
    }
}
