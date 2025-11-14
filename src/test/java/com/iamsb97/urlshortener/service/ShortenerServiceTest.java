package com.iamsb97.urlshortener.service;

import com.iamsb97.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortenerServiceTest {

    private ShortenerService urlService;

    @Mock
    private UrlRepository repository;
    @Mock
    private CacheService cache;
    @Mock
    private KeyPoolManager keyPoolManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        urlService = new ShortenerService(cache, repository, keyPoolManager);
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080/");
    }

    @Test
    void testShortenUrlSuccessfully() throws SQLException {
        String longUrl = "https://example.com/long/url";
        String shortKey = "abc123d";

        when(keyPoolManager.getNextKey()).thenReturn(shortKey);

        String result = urlService.shorten(longUrl);

        assertNotNull(result);
        assertEquals(29, result.length());
        verify(repository).save(anyString(), eq(longUrl));
    }

    @Test
    void testResolveUrlFound() {
        String longUrl = "https://example.com/long/url";
        String shortKey = "abc123d";

        when(cache.get(shortKey)).thenReturn(longUrl);

        String result = urlService.retrieve(shortKey);

        assertEquals(longUrl, result);
    }

    @Test
    void testResolveUrlNotFound() throws SQLException {
        String shortKey = "abc123d";
        String shortUrl = "http://localhost:8080/" + shortKey;

        when(cache.get(shortKey)).thenReturn(null);
        when(repository.searchLongUrl(shortUrl)).thenReturn(null);

        String notFound = urlService.retrieve(shortUrl);

        assertNull(notFound);
    }

    @Test
    void testDeleteUrl() throws SQLException {
        String shortKey = "abc123d";
        String shortUrl = "http://localhost:8080/" + shortKey;

        urlService.delete(shortUrl);
        String result = urlService.retrieve(shortUrl);

        assertNull(result);
        verify(repository).delete(shortKey);
    }
}