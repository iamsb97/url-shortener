package com.iamsb97.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.JedisPool;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class CacheConfig {

    private String host;
    private int port;

    @Bean
    public JedisPool jedisPool() {
        return new JedisPool(host, port);
    }
}
