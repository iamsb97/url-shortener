package com.iamsb97.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeyPoolManager {

    @Value("${keypool.key-length}")
    private int keyLength;

    @Value("${keypool.size}")
    private int totalPoolSize;

    private boolean isGenerating = false;
    private final String keySet = "keySet";
    private final String keyQueue = "keyQueue";
    private final CacheService cache;

    public String getNextKey() {
        
        if (isPoolLow()) {
            synchronized (this) {
                if (!isGenerating) {
                    isGenerating = true;
                    new Thread(() -> {
                        try {
                            generate();
                        } finally {
                            synchronized (this) {
                                isGenerating = false;
                            }
                        }
                    }).start();
                }
            }
        }

        return dequeueKey();
    }

    private boolean isPoolLow() {
        long poolSize = cache.getKeyPoolSize(keySet);
        return poolSize < (long) 0.1 * totalPoolSize || poolSize != -1;
    }

    private void generate() {
        while (cache.getKeyPoolSize(keySet) < totalPoolSize) {
            final String randomKeyCharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

            StringBuilder sb = new StringBuilder(keyLength);

            for (int i = 0; i < keyLength; i++) {
                int index = (int) ((randomKeyCharSet.length()) * Math.random());
                sb.append(randomKeyCharSet.charAt(index));
            }

            String newKey = sb.toString();
            if (!(cache.duplicateKey(keySet, newKey) || cache.get(newKey) != null)) {
                cache.addKeyToPool(keySet, keyQueue, newKey);
            }
        }
    }

    private String dequeueKey() {
        return cache.remKeyFromPool(keySet, keyQueue);
    }

}
