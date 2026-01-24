package com.example;

import com.example.model.Author;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;

import java.util.concurrent.TimeUnit;

/**
 * 使用R2dbcEntityTemplate请求mysql
 * 不好做join操作，适合单表查询
 */
@SpringBootTest
public class R2dbcEntityTemplateTest {
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Test
    public void test() throws InterruptedException {
        // query by criteria，即QBC查询
        Criteria criteria = Criteria.empty()
                .and("id").is(1L)
                .and("name").is("zhangsan");
        Query query = Query.query(criteria);
        r2dbcEntityTemplate.select(query, Author.class)
                .subscribe(author -> System.out.println("author is " + author));

        TimeUnit.SECONDS.sleep(3);
    }
}
