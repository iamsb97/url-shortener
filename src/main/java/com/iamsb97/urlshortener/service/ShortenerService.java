package com.iamsb97.urlshortener.service;

import java.sql.SQLException;

import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iamsb97.urlshortener.repository.URLRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortenerService {

    @Value("${shortener.key-length}")
    private int keyLength;

    @Value("${spring.application.base-url}")
    private String baseURL;

    private final CacheService cache;
    private final URLRepository repo;
    
    public String shorten(String url) {
        if (cache.get(url) != null) {
            return baseURL + cache.get(url);
        } else {
            try {
                String shortKey = repo.searchShortURL(url);
                if (shortKey != null) {
                    cache.put(shortKey, url);
                    return baseURL + shortKey;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        
        for (int retry = 0; retry < 10; retry++) {
            String key = generateRandomKey(keyLength);
            if (cache.get(key) == null) {
                try {
                    repo.save(key, url);
                    cache.put(key, url);
                    return baseURL + key;
                } catch (SQLException e) {
                    if (PSQLState.UNIQUE_VIOLATION == PSQLState.valueOf(e.getSQLState())) {
                        System.err.println("Insert failed: " + e.getMessage());
                        continue;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    public String retrieve(String shortURL) {
        String longURL = cache.get(shortURL);
        if (longURL == null) {
            try {
                longURL = repo.searchLongURL(shortURL);
                if (longURL != null) {
                    cache.put(shortURL, longURL);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return longURL;
    }

    public Boolean delete(String shortURL) {
        boolean deleted;
        
        if (cache.get(shortURL) != null) {
            cache.delete(shortURL);
        }

        try {
            deleted = repo.delete(shortURL);
            if (deleted) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String generateRandomKey(int length) {
        final String randomKeyCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int) ((randomKeyCharSet.length()) * Math.random());
            sb.append(randomKeyCharSet.charAt(index));
        }

        return sb.toString();
    }

}
