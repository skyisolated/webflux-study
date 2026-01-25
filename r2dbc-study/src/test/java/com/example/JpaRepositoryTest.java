package com.example;

import com.example.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class JpaRepositoryTest {
    @Autowired
    private AuthorRepository authorRepository;

    @Test
    public void testFindAll() throws Exception {
        authorRepository.findAll().subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(3);
    }

    /**
     * JPA可根据自定义的方法名自动生成查询的SQL，不过只适用单表操作
     * @throws InterruptedException
     */
    @Test
    public void testCustomMethod() throws InterruptedException {
        authorRepository.findAllByIdInAndNameLike(
                Arrays.asList(1L, 2L),
                "%lisi%"
        ).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(3);
    }

    /**
     * 使用@Query注解自定义SQL
     * 如果要做数据记录和自定义类的转换，可以用@Query注解 + 自定义转换器，但是这玩意覆盖范围太广，可能多个查询方法都受影响
     * 所以更推荐用DatabaseClient
     * @throws InterruptedException
     */
    @Test
    public void testQueryAnnotation() throws InterruptedException {
        authorRepository.findAllByIdAndName(1L, null).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(3);
    }
}
