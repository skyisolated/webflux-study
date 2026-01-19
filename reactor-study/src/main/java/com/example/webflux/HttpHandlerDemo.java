package com.example.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * 使用原生API编写了一个服务器
 */
@Slf4j
public class HttpHandlerDemo {
    public static void main(String[] args) throws IOException {
        HttpHandler handler = (ServerHttpRequest request, ServerHttpResponse response)->{
            URI uri = request.getURI();
            log.info("uri is {}", uri.toString());

            DataBufferFactory factory = response.bufferFactory();
            DataBuffer wrap = factory.wrap("Hello World!".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(wrap));
        };

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer.create()
                .host("localhost")
                .port(8081)
                .handle(adapter)
                .bindNow();
        log.info("server started at 8081");
        // 因为是异步，所以不这么做，已启动就立马结束了
        System.in.read();
    }
}
