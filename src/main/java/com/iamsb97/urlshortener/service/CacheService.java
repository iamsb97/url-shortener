package com.iamsb97.urlshortener.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@RequiredArgsConstructor
public class CacheService {
    
    private final JedisPool jedisPool;

    public boolean put(String shortUrl, String longUrl) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(shortUrl, longUrl);
            jedis.set(longUrl, shortUrl);
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

    public void addKeyToPool(String keySet, String keyQueue, String val) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush(keyQueue, val);
            jedis.sadd(keySet, val);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public String remKeyFromPool(String keySet, String keyQueue) {
        String val = null;

        try (Jedis jedis = jedisPool.getResource()) {
            val = jedis.rpop(keyQueue);
            jedis.srem(keySet, val);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return val;
    }

    public long getKeyPoolSize(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scard(key);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    public boolean duplicateKey(String keySet, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember(keySet, key);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return true;
    }
}
