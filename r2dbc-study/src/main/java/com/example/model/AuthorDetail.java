package com.example.model;

import java.util.List;

public class AuthorDetail {
    private Long authorId;
    private String authorName;
    private Integer authorAge;
    private List<Book> books;

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

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "AuthorDetail{" +
                "authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", authorAge=" + authorAge +
                ", books=" + books +
                '}';
    }
}
