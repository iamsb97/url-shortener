package com.iamsb97.urlshortener.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.iamsb97.urlshortener.dto.ShortenUrlResponseDto;
import com.iamsb97.urlshortener.repository.model.UrlRow;
import com.iamsb97.urlshortener.repository.UrlRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class ShortenerService {

    @Value("${spring.application.base-url}")
    private String baseUrl;

    private final CacheService cache;
    private final UrlRepository repo;
    private final DataSource dataSource;
    
    public ShortenUrlResponseDto shorten(String url) {
        String cachedKey;
        if ((cachedKey = cache.get(url)) != null) {
            return new ShortenUrlResponseDto(baseUrl + cachedKey, cachedKey);
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                UrlRow row = repo.insertLongUrl(conn, url);
                String shortKey = row.getShortKey();
                Long id = row.getId();
                String generated;
                if (shortKey == null && id != null) {
                    generated = base62encode(id);
                    int updated = repo.insertShortUrl(conn, id, generated);
                    if (updated == 0) {
                        shortKey = repo.searchShortUrl(conn, url);
                    } else {
                        shortKey = generated;
                    }
                }

                conn.commit();
                cache.put(shortKey, url);
                return new ShortenUrlResponseDto(baseUrl + shortKey, shortKey);
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String retrieve(String shortUrl) {
        String shortKey = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);
        String longUrl = cache.get(shortKey);
        if (longUrl == null) {
            try (Connection conn = dataSource.getConnection()) {
                longUrl = repo.searchLongUrl(conn, shortKey);
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

        try (Connection conn = dataSource.getConnection()) {
            deleted = repo.delete(conn, shortKey);
            if (deleted) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String base62encode(Long id) {
        final String BASE62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        final int BASE62_LENGTH = BASE62_CHARACTERS.length();

        if (id == 0) return String.valueOf(BASE62_CHARACTERS.charAt(0));
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62_CHARACTERS.charAt((int) (id % BASE62_LENGTH)));
            id /= BASE62_LENGTH;
        }
        return sb.reverse().toString();
    }

}
