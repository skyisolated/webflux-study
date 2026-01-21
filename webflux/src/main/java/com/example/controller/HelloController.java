package com.example.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class HelloController {
    /**
     * webflux很优秀的一点是向下兼容大部分的mvc注解
     * @param name
     * @return
     */
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", required = false, defaultValue = "world") String name){
        return "Hello " + name;
    }

    /**
     * 在webflux中当返回单个元素时，推荐用Mono
     * @return
     */
    @GetMapping("fruit")
    public Mono<String> fruit(){
        return Mono.just("apple");
    }

    /**
     * 在webflux中当返回多个元素时，推荐用Flux
     * @return
     */
    @GetMapping("fruits")
    public Flux<String> fruits(){
        return Flux.just("apple", "banana", "orange", "watermelon");
    }

    /**
     * sse: server sent event，服务端推送事件，像mcp协议就有用到
     * @return
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sse(){
        return Flux.range(1, 30)
                .map(item->{
                    return ServerSentEvent.builder("这是第" + item + "条数据")
                            .id(String.valueOf(item))
                            .comment("备注" + item)
                            .event("message")
                            .build();
                })
                .delayElements(Duration.ofMillis(500));

    }

    @GetMapping("/error")
    public Mono<String> error(){
        return Mono.error(new RuntimeException("This is test error"));
    }
}
