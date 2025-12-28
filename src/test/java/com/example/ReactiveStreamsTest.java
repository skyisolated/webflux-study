package com.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

/**
 * JDK9的响应式API测试，感觉就是本地化的发布订阅模型
 */
@Slf4j
public class ReactiveStreamsTest {
    @Test
    public void test() throws InterruptedException {
        Flow.Subscriber<Person> subscriber = new Flow.Subscriber<>() {
            private Flow.Subscription subscription;
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                // 订阅开启后
                log.info("onSubscribe...");
                // 获取一个元素
                subscription.request(1);
            }

            @Override
            public void onNext(Person item) {
                // 收到元素后
                log.info("onNext: {}", item.toString());
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Error ", throwable);
            }

            @Override
            public void onComplete() {
                log.info("onComplete...");
            }
        };
        // 先建立订阅关系，再发送数据，否则收不到
        SubmissionPublisher<Person> publisher = new SubmissionPublisher<>();
        publisher.subscribe(subscriber);
        for (int i = 0; i < 10; i++) {
            publisher.submit(new Person("person-" + i, i));
        }
        publisher.close();

        TimeUnit.MINUTES.sleep(1);
    }

    record Person(String name, int age) {}
}
