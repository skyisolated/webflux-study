package com.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
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

    @Test
    public void commonOperations() throws InterruptedException {
        // filter用来过滤元素
        System.out.println("\n========filter用来过滤元素=========");
        Flux.range(1, 10)
                .filter(i -> (i & 1) == 0)
                .log()
                .subscribe();

        // 当你用map()操作返回的不是一个值，而是一个集合或者数组时，可以用flatMap
        System.out.println("\n========flatMap用来将一个元素变成一个集合=========");
        Flux.just("Michael Jordon", "Tonny stark")
                .flatMap(item->{
                    String[] split = item.split(" ");
                    return Flux.fromArray(split);
                }).log().subscribe();

        // 多个流的连接
        System.out.println("\n========concat可以将多个流合成一个，支持不同类型=========");
        Flux.concat(Flux.just("1", "2", "3"), Flux.just(4,5,6), Flux.just("test"))
                .log()
                .subscribe();

        System.out.println("\n========concatWith也可以合并流，但元素类型必须相同=========");
        Flux.range(1, 3)
                .concatWith(Flux.just(4, 5))
                .log()
                .subscribe();

        // transform和transformDeferred可以转换整个流
        System.out.println("\n========transform可以转换整个流，不共享变量=========");
        AtomicInteger count = new AtomicInteger(0);
        Flux<String> flux = Flux.just("a", "b", "c")
                .transform(values -> {
                    if (count.incrementAndGet() == 1) {
                        return values.map(String::toUpperCase);
                    } else {
                        return values;
                    }
                });
        flux.subscribe(item->System.out.println("订阅者1：" + item));
        flux.subscribe(item->System.out.println("订阅者2：" + item));

        System.out.println("\n========transformDeferred可以转换整个流，共享变量=========");
        AtomicInteger cnt = new AtomicInteger(0);
        Flux<String> deferred = Flux.just("a", "b", "c")
                .transformDeferred(values -> {
                    if (cnt.incrementAndGet() == 1) {
                        return values.map(String::toUpperCase);
                    } else {
                        return values;
                    }
                });
        deferred.subscribe(item->System.out.println("订阅者1：" + item));
        deferred.subscribe(item->System.out.println("订阅者2：" + item));


        System.out.println("\n========defaultIfEmpty和switchIfEmpty可以在流为空的时候设置默认值=========");
        // Flux.empty()是真正的空流，而Flux.just(null)是包含一个null元素的流
        Flux.empty().defaultIfEmpty("aaa")
                .log()
                .subscribe();
        Flux.empty().switchIfEmpty(Flux.just("bbb"))
                .log()
                .subscribe();

        // merge合并流，与concat的区别是，concat是将流进行连接，而merge是按元素接收的时间顺序对流进行合并
        System.out.println("\n========merge合并流=========");
        Flux.merge(
                Flux.just(1, 2).delayElements(Duration.ofSeconds(2)),
                Flux.just(3, 4).delayElements(Duration.ofSeconds(1)))
                .log()
                .subscribe();
        TimeUnit.SECONDS.sleep(6);

        // zip，看起来是压缩，不如说是配对
        System.out.println("\n========zip，看起来是压缩，不如说是配对=========");
        // zip会取每个流对应位置的元素组成元组，如果流的元素个数不统一，那么多出来的元素会被抛弃
        Flux.zip(Flux.just(1, 2), Flux.just(4, 5), Flux.just(7,8,9))
                .log().subscribe();
    }

    @Test
    public void handleError(){
        System.out.println("\n========onErrorReturn处理异常=========");
        // onErrorReturn会吃掉异常，消费者感知不到；会返回一个默认值；流在此会正常结束，不会处理后续的元素
        Flux.just(1, 2, 0, 3)
                .map(i -> 100 / i)
                .onErrorReturn(0)
                .subscribe(System.out::println);


        System.out.println("\n========onErrorResume处理异常=========");
        // onErrorResume会吃掉异常(看你是否重新抛了)，消费者感知不到；会执行一个回调方法；流在此会正常结束，不会处理后续的元素
        Flux.just(1, 2, 0, 3)
                .map(i -> 100 / i)
                .onErrorResume(e -> Flux.just(0))
//                .onErrorResume(e -> Flux.error(new RuntimeException("异常处理")))
                .subscribe(System.out::println);

        System.out.println("\n========onErrorMap处理异常=========");
        // onErrorMap可以将一种异常转换成另一种异常，消费者可以感知到。
        Flux.just(1, 2, 0, 3)
                .map(i -> 100 / i)
                .onErrorMap(e -> new RuntimeException("异常处理"))
                .subscribe((value)-> System.out.println(value), e -> System.out.println("异常处理"));

        System.out.println("\n========doOnError处理异常=========");
        // doOnError就是在异常时做一件事，不会吃掉异常，消费者可感知。
        Flux.just(1, 2, 0, 3)
                .map(i -> 100 / i)
                .doOnError(e -> System.out.println("doOnError： " + e.getMessage()))
                .subscribe((value)-> System.out.println(value),
                        e -> System.out.println("异常处理: " + e.getMessage()));

        System.out.println("\n========onErrorContinue处理异常=========");
        // onErrorContinue，发生异常继续执行，消费者感知不到
        Flux.just(1, 2, 0, 3, 4)
                .map(i -> 100 / i)
                .onErrorContinue((ex, value)->{
                    System.out.println("检测到错误！");
                    System.out.println("值为:" +  value);
                    System.out.println("异常为:" +  ex.getMessage());
                }).subscribe(value -> System.out.println(value),
                    err-> System.out.println("异常处理: " + err.getMessage())
                );

        System.out.println("\n========onErrorComplete处理异常=========");
        // onErrorComplete，将错误结束信号替换为正常结束信号
        Flux.just(1, 2, 0, 3, 4)
                .map(i -> 100 / i)
                .onErrorComplete()
                .subscribe(System.out::println);

        System.out.println("\n========onErrorStop处理异常=========");
        // onErrorStop，错误后从源头上停止流，所以订阅者都会受影响
        Flux.just(1, 2, 0, 3, 4)
                .map(i -> 100 / i)
                .onErrorStop()
                .subscribe(System.out::println);
    }

    @Test
    public void retry() throws InterruptedException {
        // timeout设置超时时间，retry设置重试次数，它会将流从头到尾重新请求一次
        Flux.just(1)
                .log()
                .delayElements(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(2))
                .retry(3)
                .map(i ->  i * 2 )
                .subscribe();
        TimeUnit.SECONDS.sleep(7);
    }

    @Test
    public void sinks() throws InterruptedException {
        // 流有冷热之分，一般来说用Flux.just()等方法创建的是冷流，而用本章Sinks的方法创建的就是热流
        // 冷流的每个订阅者无论订阅的顺序如何，都会从头开始获取完整数据
        // 热流的订阅者拿不到之前的数据，只能获取订阅后的数据。可以用cache或者replay让后订阅者拿到历史数据
        Sinks.many(); // 相当于一个Flux
        Sinks.one(); // 相当于一个Mono

        Sinks.many().unicast(); // 单播，只能被一个消费者订阅
        Sinks.many().multicast(); // 广播，可被多个消费者订阅
        Sinks.many().replay(); // 重放，能够让不同时间订阅的消费者都能从头开始订阅数据

        Sinks.Many<Object> all = Sinks.many().replay().all();
//        Sinks.Many<Object> all = Sinks.many().multicast().onBackpressureBuffer();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                all.tryEmitNext(i);
            }
        }).start();

        all.asFlux().subscribe(value-> System.out.println("订阅者1：" + value));
        TimeUnit.SECONDS.sleep(1);
        all.asFlux().subscribe(value-> System.out.println("订阅者2：" + value));
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * 使用blockAPI可以将异步的流变回阻塞式的操作
     */
    @Test
    public void block(){
        Flux<Integer> flux = Flux.range(1, 10)
                .map(i -> i * 2);

        Integer first = flux.blockFirst();
        Integer last = flux.blockLast();
        List<Integer> list = flux.collectList().block();
        System.out.println("第一个元素是" + first);
        System.out.println("最后一个元素是" + last);
        System.out.println("所有元素是" + list);
    }

    @Test
    public void parallel() throws InterruptedException {
        // 并发批处理流
        Flux.range(1, 100)
                .buffer(10)
                .parallel(4)
                .runOn(Schedulers.newParallel("xxx"))
                .log()
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(5);
    }

    /**
     * 在响应式编程中，ThreadLocal会失效，因为流的每个操作可能会切换线程。
     * 因此contextAPI就是为了解决这个问题，其与流绑定而不是线程
     * 但用起来还是觉得很别扭，要套很多层
     * @throws InterruptedException
     */
    @Test
    public void contextAPI() throws InterruptedException {
        // context是从下游往上游传
        // 在springmvc中，数据从controller --> service --> dao
        // 而在响应式编程中， dao --> service --> controller，-->表示订阅关系，dao如何得知参数，由controller从下游往上游传播
        Mono<String> mono =
                Mono.just("hello")
                        .flatMap(v ->
                                Mono.deferContextual(ctx ->
                                        Mono.just(v + ", user=" + ctx.get("user"))
                                )
                        )
                        // context由下游传到上游
                        .contextWrite(Context.of("user", "sky"));

        mono.subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(3);

    }
}
