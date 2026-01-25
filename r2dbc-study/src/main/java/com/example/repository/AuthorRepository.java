package com.example.repository;

import com.example.model.Author;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * 类似mybatis-plus里面的BaseMapper，继承后就有了基本的CRUD方法
 * 也可以继承ReactiveCrudRepository，这是这个提供的方法有点少
 */
@Repository
public interface AuthorRepository extends R2dbcRepository<Author, Long> {
    // JPA的机制，会根据自定义的方法名自动生成查询的SQL
    Flux<Author> findAllByIdInAndNameLike(Collection<Long> ids, String name);

    @Query("select * from author where id=:id or name=:name")
    Flux<Author> findAllByIdAndName(@Param("id") Long id, @Param("name") String name);
}
