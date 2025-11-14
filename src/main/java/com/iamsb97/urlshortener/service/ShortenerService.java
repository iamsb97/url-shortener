package com.iamsb97.urlshortener.service;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iamsb97.urlshortener.repository.UrlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortenerService {

    @Value("${spring.application.base-url}")
    private String baseUrl;

    private final CacheService cache;
    private final UrlRepository repo;
    private final KeyPoolManager keyPool;
    
    public String shorten(String url) {
        if (cache.get(url) != null) {
            return baseUrl + cache.get(url);
        } else {
            try {
                String shortKey = repo.searchShortUrl(url);
                if (shortKey != null) {
                    cache.put(shortKey, url);
                    return baseUrl + shortKey;
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
                return baseUrl + key;
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

    public String retrieve(String shortUrl) {
        String shortKey = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);
        String longUrl = cache.get(shortKey);
        if (longUrl == null) {
            try {
                longUrl = repo.searchLongUrl(shortKey);
                if (longUrl != null) {
                    cache.put(shortKey, longUrl);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return longUrl;
    }

    public Boolean delete(String shortUrl) {
        boolean deleted;
        String shortKey = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);
        
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
