package com.example.model;

import java.time.Instant;
import java.util.List;

public class BookInfo {
    private Long bookId;
    private String bookName;
    private Instant publishTime; // 在响应式编程中，日期类型不要用Date，用Instant或者LocalXxx
    private Long authorId;
    private String authorName;
    private Integer authorAge;
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public Instant getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Instant publishTime) {
        this.publishTime = publishTime;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getAuthorAge() {
        return authorAge;
    }

    public void setAuthorAge(Integer authorAge) {
        this.authorAge = authorAge;
    }

    @Override
    public String toString() {
        return "BookInfo{" +
                "bookId=" + bookId +
                ", bookName='" + bookName + '\'' +
                ", publishTime=" + publishTime +
                ", authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", authorAge=" + authorAge +
                '}';
    }
}
