package com.example;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReactorTest {
    /**
     * Flux是包含N个元素的流，其本身实现了Publisher接口
     * @throws IOException
     */
    @Test
    public void fluxTest() throws IOException {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5, 6);
        // doOnXxx是流在对应时机的回调
        flux.doOnComplete(()->{
            System.out.println("完成");
        }).doOnNext(item -> System.out.println("Received: " + item));
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

    /**
     * range: 类似python中的range，生成一个序列
     * log：打印日志
     */
    @Test
    public void range(){
        // 类似于python中的range
        Flux.range(1, 10)
                .doOnComplete(() -> System.out.println("Completed"))
                .subscribe(item -> System.out.println("Received: " + item));

        Flux<Integer> range = Flux.range(1, 10);
        Flux<Integer> range1 = Flux.range(-1, 0);
        // 组合两个流
        range.concat(range1)
                .filter(x -> x > 0)
                // 打印日志
                .log()
                .subscribe(item -> System.out.println("Received: " + item));
    }

    /**
     * subscribe方法的使用
     */
    @Test
    public void subscribe(){
        // 尝试一下doOnXxx
        System.out.println("\n============尝试一下doOnXxx========");
        Flux.range(1, 20)
                .map(item -> {
                    if (item == 5) {
                        throw new RuntimeException("5 is not allowed");
                    }
                    return item * 2;
                })
                .doOnError(ex ->{
                    System.out.println("发生异常: " + ex.getMessage());
                })
                .subscribe(System.out::println);

        System.out.println("\n============subscribe最多可以传三个Consumer========");
        // subscribe，订阅这个流，类似jdk8 stream的结束操作。如果一个流不被订阅，那么它什么也不会做
        Flux<Integer> flux = Flux.range(1, 20)
                .map(item -> {
                    if (item == 5) {
                        throw new RuntimeException("5 is not allowed");
                    }
                    return item;
                });

        // subscribe最多可以传三个Consumer：处理成功，处理异常，处理完成
        flux.subscribe(
                item->{
                    System.out.println("Received: " + item);
                },
                ex->{
                    System.out.println("发生异常: " + ex.getMessage());
                },
                ()->{
                    System.out.println("完成");
                }
        );

        // subscribe也可以传入自定义的SubScriber
        System.out.println("============subscribe也可以传入自定义的SubScriber========");
        Flux<Integer> range = Flux.range(1, 20)
                        .map(item -> {
                            if (item == 15) {
                                throw new RuntimeException("5 is not allowed");
                            }
                            return item;
                        }).doOnError(ex->{
                            System.out.println("发生异常: " + ex.getMessage());
                        }).onErrorComplete();
        // 这里需要注意，doOnXxx只是给你一个通知，你无法修改出错的元素。而onErrorXxx功能更为强大，可以改变元素或者信号
        // 比如在此处，onErrorComplete可以将报错吃掉，这样下方的BaseSubscriber中就感知不到流出错了，认为流是正常结束。

        // BaseSubscriber是官方给你封装好的一个Subscriber，直接编写它的子类即可。
        range.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println("检测到了订阅，开始请求元素");
//                request(1); // 请求一次数据
                requestUnbounded(); // 要多次数据，不执行request的话收不到元素
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println("收到元素：" + value);
                if (value == 10){
                    cancel();
                }
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("流正常结束了");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                System.out.println("流异常结束了, " + throwable.getMessage());
            }

            @Override
            protected void hookOnCancel() {
                System.out.println("流被取消了");
            }

            @Override
            protected void hookFinally(SignalType type) {
                System.out.println("流最终的操作");
            }
        });
    }

    /**
     * buffer: 批量处理元素
     */
    @Test
    public void buffer(){
        // buffer的作用是批量处理元素，设置size大小，当消费者请求时，一次发送size个数据过去
        Flux<List<Integer>> buffer = Flux.range(1, 10)
                .buffer(4);
        buffer.subscribe(new BaseSubscriber<List<Integer>>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // request严格意义上讲是请求一次数据，至于这一次是多少个数据，取决于buffer的大小
                // 如果没设置buffer，那么一次请求一个，每个收到一个元素，类型是对应的类型
                // 如果设置了buffer，即便大小是1，每次收到的元素也是一个数组。
                request(1);
            }

            @Override
            protected void hookOnNext(List<Integer> values) {
                System.out.println("收到数据: " + values);
                request(1);
            }
        });
    }

    /**
     * limitRate限制消费者的速度
     */
    @Test
    public void limitRate(){
        // limitRate用于限制消费者的消费速率，第一次会获取指定速率的元素，后面按75%的比例获取
        // 以下面的代码为例：第一次先request 100个，之后每次request 75个。75%是一个预取因子。
        Flux.range(1, 1000)
                .log()
                .limitRate(100)
                .subscribe();
    }

    /**
     * generate: 自己用generate，以同步的方式实现一个从0-10的流。
     * 如果是异步的场景，可以用create，但较为复杂，这里不做介绍
     */
    @Test
    public void generate(){
        // 自己用generate，以同步的方式实现一个从0-10的流。
        // sink可以译为水槽、管道或接收器
        Flux<Object> flux = Flux.generate(() -> 0, (state, sink) -> {
            if (state <= 10) {
                sink.next(state); // 将元素放入通道
            } else {
                sink.complete(); // 完成
            }
            return state + 1;
        });

        flux.log().subscribe();
    }
    /**
     * handle: 可以自定义流的处理逻辑
     */
    @Test
    public void handle(){
        // handle可以自定义处理器，和map相比更灵活一些
        Flux.range(1, 20)
                .log()
                .handle((item, sink) -> {
                    System.out.println("拿到了数据:" + item + "，将其+1再传递");
                    sink.next(item + 1); // 向通道放入元素
                })
                .subscribe();
    }

    /**
     * Schedulers就像Executors，提供了几种线程池，你可以在指定订阅者和发布者的所使用的线程池。
     * @throws InterruptedException
     */
    @Test
    public void scheduler() throws InterruptedException {
        // 默认情况下，整个Reactor链都运行在调用subscribe()的线程上（通常是main线程）。
        // subscribeOn，位置不重要，但只有第一个有效
        // publishOn，从该点开始，后续操作在指定的Scheduler上运行。可以多次使用，每次都会切换线程。
        Scheduler single = Schedulers.single();
        Scheduler boundededElastic = Schedulers.boundedElastic();

        Flux.range(1, 20)
                .log()
                .publishOn(boundededElastic)
                // 具体深入的话还比较复杂，这里暂时不做深入了
                .subscribeOn(boundededElastic)
                .subscribe(System.out::println);

        TimeUnit.MINUTES.sleep(1);
    }

}
