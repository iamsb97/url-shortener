package com.iamsb97.urlshortener.repository.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UrlRow {
    Long id;
    String shortKey;
    String LongUrl;
    Timestamp createdAt;
    Timestamp expiresAt;

    public UrlRow (Long id, String shortKey) {
        this.id = id;
        this.shortKey = shortKey;
    }
}
