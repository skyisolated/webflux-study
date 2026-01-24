package com.example;

import com.example.model.Author;
import io.asyncer.r2dbc.mysql.MySqlConnection;
import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class R2dbcTest {
    /**
     * 尝试用最原生的API读写mysql，感觉挺麻烦的
     * @throws Exception
     */
    @Test
    public void testConnection() throws Exception {
        // 连接配置
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
                .host("localhost")
                .port(3306)
                .user("root")
                .password("2933")
                .database("test")
                .serverZoneId(ZoneId.of(TimeZone.getTimeZone("GMT+8").getID()))
                .build();

        // 创建连接工厂
        MySqlConnectionFactory connectionFactory = MySqlConnectionFactory.from(configuration);
        Mono<MySqlConnection> connectionMono = connectionFactory.create();
        // Mono和Flux的from方法可以基于现有的数据流创建新的流
        Mono.from(connectionMono)
            // flatMapMany是将一个Mono转换成一个Flux，这里要基于一个连接获取一个结果集
            .flatMapMany(connection ->
                connection.createStatement("select * from author where id=?id and name=?name")
                        .bind("id", 1) // 这是具名参数，在statement中用?param指定
                        .bind("name", "zhangsan")
                        .execute()
                    // 这里将每个结果集中的记录映射为Author对象
            ).flatMap(result ->
                    result.map(readable -> {
                        Long id = readable.get("id", Long.class);
                        String name = readable.get("name", String.class);
                        Integer age = readable.get("age", Integer.class);
                        Author author = new Author();
                        author.setId(id);
                        author.setName(name);
                        author.setAge(age);
                        return author;
                    })
                ).subscribe(author -> System.out.println("author is " + author));

        TimeUnit.SECONDS.sleep(3);
    }
}
