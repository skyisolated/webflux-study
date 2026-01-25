package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("book")
public class Book {
    @Id
    private Long id;
    @Column("name")
    private String bookName;
    @Column("author_id")
    private Long authorId;
    @Column("publish_time")
    private Instant publishTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Instant getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Instant publishTime) {
        this.publishTime = publishTime;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", bookName='" + bookName + '\'' +
                ", authorId=" + authorId +
                ", publishTime=" + publishTime +
                '}';
    }
}
