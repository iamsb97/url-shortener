package com.iamsb97.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortenerService {

    @Value("${shortener.key-length}")
    private int keyLength;

    @Value("${app.base-url}")
    private String baseURL;

    private final CacheService cache;
    
    public String shorten(String url) {
        if (cache.get(url) != null) {
            return baseURL + cache.get(url);
        }
        
        for (int retry = 0; retry < 10; retry++) {
            String key = generateRandomKey(keyLength);
            if (cache.get(key) == null) {
                cache.put(key, url);
                cache.put(url, key);
                return baseURL + key;
            }
        }

        return null;
    }

    public String retrieve(String shortURL) {
        String longURL = cache.get(shortURL);
        return longURL;
    }

    public Boolean delete(String shortURL) {
        if (cache.get(shortURL) != null) {
            cache.delete(shortURL);
            return true;
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
