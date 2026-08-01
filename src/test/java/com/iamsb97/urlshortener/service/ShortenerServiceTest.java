package com.iamsb97.urlshortener.service;

import com.iamsb97.urlshortener.dto.ShortenUrlResponseDto;
import com.iamsb97.urlshortener.repository.UrlRepository;
import com.iamsb97.urlshortener.repository.model.UrlRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortenerServiceTest {

    private static final String BASE_URL = "http://localhost:8080/";
    private static final String LONG_URL = "https://example.com/long/url";
    private static final String SHORT_KEY = "1z";

    private ShortenerService shortenerService;

    @Mock
    private UrlRepository repository;
    @Mock
    private CacheService cache;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        shortenerService = new ShortenerService(cache, repository, dataSource);
        ReflectionTestUtils.setField(shortenerService, "baseUrl", BASE_URL);
    }

    @Test
    void shortenReturnsCachedShortUrl() throws SQLException {
        when(cache.get(LONG_URL)).thenReturn(SHORT_KEY);

        ShortenUrlResponseDto result = shortenerService.shorten(LONG_URL);

        assertNotNull(result);
        assertEquals(BASE_URL + SHORT_KEY, result.getShortUrl());
        assertEquals(SHORT_KEY, result.getShortKey());
        verify(dataSource, never()).getConnection();
    }

    @Test
    void shortenCreatesAndCachesNewShortKey() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(repository.insertLongUrl(connection, LONG_URL)).thenReturn(new UrlRow(123L, null));
        when(repository.insertShortUrl(connection, 123L, SHORT_KEY)).thenReturn(1);

        ShortenUrlResponseDto result = shortenerService.shorten(LONG_URL);

        assertNotNull(result);
        assertEquals(BASE_URL + SHORT_KEY, result.getShortUrl());
        assertEquals(SHORT_KEY, result.getShortKey());
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(cache).put(SHORT_KEY, LONG_URL);
    }

    @Test
    void shortenReturnsExistingDatabaseShortKey() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(repository.insertLongUrl(connection, LONG_URL)).thenReturn(new UrlRow(123L, "existing"));

        ShortenUrlResponseDto result = shortenerService.shorten(LONG_URL);

        assertNotNull(result);
        assertEquals(BASE_URL + "existing", result.getShortUrl());
        verify(repository, never()).insertShortUrl(connection, 123L, "existing");
        verify(connection).commit();
        verify(cache).put("existing", LONG_URL);
    }

    @Test
    void retrieveReturnsCachedLongUrl() throws SQLException {
        when(cache.get(SHORT_KEY)).thenReturn(LONG_URL);

        String result = shortenerService.retrieve(SHORT_KEY);

        assertEquals(LONG_URL, result);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void retrieveLoadsAndCachesDatabaseValue() throws SQLException {
        when(cache.get(SHORT_KEY)).thenReturn(null);
        when(dataSource.getConnection()).thenReturn(connection);
        when(repository.searchLongUrl(connection, SHORT_KEY)).thenReturn(LONG_URL);

        String result = shortenerService.retrieve(SHORT_KEY);

        assertEquals(LONG_URL, result);
        verify(cache).put(SHORT_KEY, LONG_URL);
    }

    @Test
    void deleteEvictsCacheAndDeletesDatabaseRecord() throws SQLException {
        when(cache.get(SHORT_KEY)).thenReturn(LONG_URL);
        when(dataSource.getConnection()).thenReturn(connection);
        when(repository.delete(connection, SHORT_KEY)).thenReturn(true);

        boolean deleted = shortenerService.delete(SHORT_KEY);

        assertTrue(deleted);
        verify(cache).delete(SHORT_KEY);
        verify(repository).delete(connection, SHORT_KEY);
    }

    @Test
    void deleteReturnsFalseWhenDatabaseRecordIsMissing() throws SQLException {
        when(cache.get(SHORT_KEY)).thenReturn(null);
        when(dataSource.getConnection()).thenReturn(connection);
        when(repository.delete(connection, SHORT_KEY)).thenReturn(false);

        boolean deleted = shortenerService.delete(SHORT_KEY);

        assertFalse(deleted);
        verify(cache, never()).delete(SHORT_KEY);
    }
}
