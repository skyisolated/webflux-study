package com.example.filter;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class MyWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 方法执行前的处理
        Mono<Void> filter = chain.filter(exchange);
        // 流经过一个操作会变成新流，所以下方一定要返回新的流
        Mono<Void> newFilter = filter.doFinally(signalType -> {
            System.out.println("这里才是目标方法执行后的处理");
        });
        System.out.println("这里并不是目标方法执行后的操作");
        return newFilter;
    }
}
