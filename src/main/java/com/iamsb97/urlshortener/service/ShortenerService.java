package com.iamsb97.urlshortener.service;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iamsb97.urlshortener.repository.URLRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortenerService {

    @Value("${spring.application.base-url}")
    private String baseURL;

    private final CacheService cache;
    private final URLRepository repo;
    private final KeyPoolManager keyPool;
    
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
            String key = keyPool.getNextKey();

            if (key == null) return null;

            try {
                repo.save(key, url);
                cache.put(key, url);
                return baseURL + key;
            } catch (SQLException e) {
                if ("23505" == e.getSQLState()) {
                    System.err.println("Insert failed: " + e.getMessage());
                } else {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public String retrieve(String shortURL) {
        String shortKey = shortURL.substring(shortURL.lastIndexOf('/') + 1);
        String longURL = cache.get(shortKey);
        if (longURL == null) {
            try {
                longURL = repo.searchLongURL(shortKey);
                if (longURL != null) {
                    cache.put(shortKey, longURL);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return longURL;
    }

    public Boolean delete(String shortURL) {
        boolean deleted;
        String shortKey = shortURL.substring(shortURL.lastIndexOf('/') + 1);
        
        if (cache.get(shortKey) != null) {
            cache.delete(shortKey);
        }

        try {
            deleted = repo.delete(shortKey);
            if (deleted) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
