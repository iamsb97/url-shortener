package com.iamsb97.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheServiceTest {

    private CacheService cacheService;

    @Mock
    private JedisPool jedisPool;
    @Mock
    private Jedis jedis;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jedisPool.getResource()).thenReturn(jedis);
        cacheService = new CacheService(jedisPool);
    }

    @Test
    void testPutStoresBothShortAndLongUrl() {
        String shortUrl = "abc123";
        String longUrl = "https://example.com";

        boolean result = cacheService.put(shortUrl, longUrl);

        assertTrue(result);
        verify(jedis).set(shortUrl, longUrl);
        verify(jedis).set(longUrl, shortUrl);
        verify(jedis).close();
    }

    @Test
    void testPutHandlesExceptionGracefully() {
        when(jedisPool.getResource()).thenThrow(new RuntimeException("Redis down"));

        boolean result = cacheService.put("a", "b");

        assertFalse(result);
    }

    @Test
    void testGetReturnsCachedValue() {
        String key = "abc123";
        String expectedValue = "https://example.com";
        when(jedis.get(key)).thenReturn(expectedValue);

        String result = cacheService.get(key);

        assertEquals(expectedValue, result);
        verify(jedis).close();
    }

    @Test
    void testGetReturnsNullOnException() {
        when(jedisPool.getResource()).thenThrow(new RuntimeException("Boom"));

        String result = cacheService.get("key");

        assertNull(result);
    }

    @Test
    void testDeleteRemovesBothKeysWhenPresent() {
        String key = "abc123";
        String value = "https://example.com";

        when(jedis.get(key)).thenReturn(value);

        boolean result = cacheService.delete(key);

        assertTrue(result);
        verify(jedis).del(key);
        verify(jedis).del(value);
        verify(jedis).close();
    }

    @Test
    void testDeleteDoesNothingIfKeyMissing() {
        when(jedis.get("abc")).thenReturn(null);

        boolean result = cacheService.delete("abc");

        assertTrue(result);
        verify(jedis, never()).del(anyString());
        verify(jedis).close();
    }

}
