package com.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.example.model.Author;
import com.example.model.AuthorDetail;
import com.example.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    /**
     * 使用collectList做一对多的join查询，并将结果封装成自定义的类
     * 带查询条件，结果必定都是一条记录的。
     * @throws InterruptedException
     */
    @Test
    public void testOneToNByCollectList() throws InterruptedException {
        client.sql("SELECT author.*, book.id as book_id, book.book_name, book.publish_time from author " +
                "INNER JOIN book on author.id = book.author_id " +
                "where author.name = ?authorName; ")
                .bind("authorName", "zhangsan")
                .fetch()
                .all()
                .collectList() // 这里是关键，一次性获取流中的所有元素，变成一个Mono<List<>>
                .map(list->{
                    if (list.isEmpty()){
                        return null;
                    }
                    Map<String, Object> firstItem = list.get(0);
                    AuthorDetail authorDetail = new AuthorDetail();
                    Long id = Long.valueOf(firstItem.get("id").toString());
                    String name = (String) firstItem.get("name");
                    Integer age = (Integer) firstItem.get("age");
                    authorDetail.setAuthorId(id);
                    authorDetail.setAuthorName(name);
                    authorDetail.setAuthorAge(age);
                    List<Book> collect = list.stream().map(item -> {
                        Long bookId = Long.valueOf(item.get("book_id").toString());
                        String bookName = (String) item.get("book_name");
                        ZonedDateTime zonedDateTime = (ZonedDateTime) item.get("publish_time");
                        Instant publishTime = zonedDateTime.toInstant();
                        Book target = new Book();
                        target.setId(bookId);
                        target.setBookName(bookName);
                        target.setPublishTime(publishTime);
                        return target;
                    }).collect(Collectors.toList());
                    authorDetail.setBooks(collect);
                    return authorDetail;
                }).subscribe(authorDetail ->
                        System.out.println("authorDetail = " + JSON.toJSONString(authorDetail, JSONWriter.Feature.PrettyFormat)
                ));

        TimeUnit.SECONDS.sleep(3);
    }

    /**
     * 使用bufferUntilChanged做一对多的join查询，并将结果封装成自定义的类.
     * 使用bufferUntilChanged，sql必须排序！
     * 这里没带查询条件，结果可以是多条记录的。
     * @throws InterruptedException
     */
    @Test
    public void testOneToNByBufferUntilChanged() throws InterruptedException {
        client.sql("SELECT author.*, book.id as book_id, book.book_name, book.publish_time from author " +
                        "INNER JOIN book on author.id = book.author_id ORDER BY id desc")
                .fetch()
                .all()
                // 自己设定分组规则，这里是当记录的id发生变化，就换一个新的buffer
                .bufferUntilChanged(item -> {
                    Object id = item.get("id");
                    long value = Long.valueOf(id.toString()).longValue();
                    return value;
                })
                .map(list->{
                    if (list.isEmpty()){
                        return null;
                    }
                    Map<String, Object> firstItem = list.get(0);
                    AuthorDetail authorDetail = new AuthorDetail();
                    Long id = Long.valueOf(firstItem.get("id").toString());
                    String name = (String) firstItem.get("name");
                    Integer age = (Integer) firstItem.get("age");
                    authorDetail.setAuthorId(id);
                    authorDetail.setAuthorName(name);
                    authorDetail.setAuthorAge(age);
                    List<Book> collect = list.stream().map(item -> {
                        Long bookId = Long.valueOf(item.get("book_id").toString());
                        String bookName = (String) item.get("book_name");
                        ZonedDateTime zonedDateTime = (ZonedDateTime) item.get("publish_time");
                        Instant publishTime = zonedDateTime.toInstant();
                        Book target = new Book();
                        target.setId(bookId);
                        target.setBookName(bookName);
                        target.setPublishTime(publishTime);
                        return target;
                    }).collect(Collectors.toList());
                    authorDetail.setBooks(collect);
                    return authorDetail;
                }).subscribe(authorDetail ->
                        System.out.println("authorDetail = " + JSON.toJSONString(authorDetail, JSONWriter.Feature.PrettyFormat)
                        ));

        TimeUnit.SECONDS.sleep(3);
    }

    /**
     * 使用groupBy做一对多的join查询，并将结果封装成自定义的类。
     * 注意一点：groupBy之后要转换必须用flatMap，不能用map
     * 当你的 lambda 返回的是 Mono / Flux（任何 Publisher），就必须用 flatMap；返回的是普通对象，用 map。
     * @throws InterruptedException
     */
    @Test
    public void testOneToNByGroupBy() throws InterruptedException {
                client.sql("SELECT author.*, book.id as book_id, book.book_name, book.publish_time from author " +
                        "INNER JOIN book on author.id = book.author_id;")
                .fetch()
                .all()
                // 这里按照id进行分组，下面flatMap中的每个元素就是每个id对应的记录集合
                .groupBy(row -> Long.valueOf(row.get("id").toString()))
                // 需要注意这里必须用flatMap，如果上游返回了Publisher必须用flatMap，普通对象可以用map
                .flatMap(group->
                        // 每个group是每个id所对应的记录集合，使用collectList()拿到所有记录，讲所有记录封装成一个类
                    group.collectList().map(list->{
                        Map<String, Object> firstItem = list.get(0);
                        AuthorDetail authorDetail = new AuthorDetail();
                        Long id = Long.valueOf(firstItem.get("id").toString());
                        String name = (String) firstItem.get("name");
                        Integer age = (Integer) firstItem.get("age");
                        authorDetail.setAuthorId(id);
                        authorDetail.setAuthorName(name);
                        authorDetail.setAuthorAge(age);
                        List<Book> collect = list.stream().map(item -> {
                            Long bookId = Long.valueOf(item.get("book_id").toString());
                            String bookName = (String) item.get("book_name");
                            ZonedDateTime zonedDateTime = (ZonedDateTime) item.get("publish_time");
                            Instant publishTime = zonedDateTime.toInstant();
                            Book target = new Book();
                            target.setId(bookId);
                            target.setBookName(bookName);
                            target.setPublishTime(publishTime);
                            return target;
                        }).collect(Collectors.toList());
                        authorDetail.setBooks(collect);
                        return authorDetail;
                    })
                ).subscribe(authorDetail -> System.out.println("authorDetail = " + JSON.toJSONString(authorDetail, JSONWriter.Feature.PrettyFormat)));

        TimeUnit.SECONDS.sleep(3);
    }
}
