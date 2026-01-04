package com.example;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;

public class ReactorTest {
    /**
     * Flux是包含N个元素的流，其本身实现了Publisher接口
     * @throws IOException
     */
    @Test
    public void fluxTest() throws IOException {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5, 6);

        // 流可以有多个消费者
        flux.subscribe(item -> System.out.println("Received: " + item));
        flux.subscribe(item -> System.out.println("Received: " + item));
        System.out.println("========================");

        // 从0开始，每隔一秒 + 1
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
        interval.subscribe(item -> System.out.println("Received: " + item));

        System.in.read();
    }

    /**
     * Mono是包含0或1个元素的流，其本身实现了Publisher接口
     * @throws IOException
     */
    @Test
    public void monoTest() throws IOException {
        Mono<Integer> mono = Mono.just(1);
        mono.subscribe(item -> System.out.println("Received: " + item));
        System.in.read();
    }
}
