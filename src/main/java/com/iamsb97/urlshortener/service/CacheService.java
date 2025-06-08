package com.iamsb97.urlshortener.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@RequiredArgsConstructor
public class CacheService {
    
    private final JedisPool jedisPool;

    public boolean put(String shortURL, String longURL) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(shortURL, longURL);
            jedis.set(longURL, shortURL);
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public boolean delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            if (value != null) {
                jedis.del(key);
                jedis.del(value);
            }
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
