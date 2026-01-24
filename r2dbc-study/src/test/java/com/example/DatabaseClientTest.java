package com.example;

import com.example.model.Author;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.concurrent.TimeUnit;

/**
 * 使用DatabaseClient请求mysql
 * 相较于R2dbcEntityTemplate，DatabaseClient更贴近于底层，使用复杂查询
 * 毕竟是直接写sql
 */
@SpringBootTest
public class DatabaseClientTest {
    @Autowired
    private DatabaseClient client;
    @Test
    public void test() throws InterruptedException {
        client.sql("select * from author where id=?id or name=?name")
                .bind("id", 1L) // 具名参数
                .bind("name", "lisi")
                .fetch() // fetch即获取结果，有all()，first()和one()三种操作
                .all() // 返回的每条记录是个Map
                .map(item -> {
                    String id = item.get("id").toString();
                    String name = item.get("name").toString();
                    String age = item.get("age").toString();
                    Author author = new Author();
                    author.setId(Long.parseLong(id));
                    author.setName(name);
                    author.setAge(Integer.parseInt(age));
                    return author;
                }).subscribe(author -> System.out.println("author = " + author));
        TimeUnit.SECONDS.sleep(4);
    }
}
