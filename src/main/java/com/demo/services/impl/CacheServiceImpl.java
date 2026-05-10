package com.demo.services.impl;

import com.demo.services.CacheService;
import com.demo.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {
    private final RedisTemplate redisTemplate;
    private final String urlKey = AppConstants.URL_KEY;
    private final String urlViewsKey = AppConstants.URL_VIEWS_KEY;

    @Override
    public void cacheUrl(String shortCode, String baseUrl, Duration ttl) {
        redisTemplate.opsForValue().set(urlKey + shortCode, baseUrl, ttl);
    }

    @Override
    public String getCachedUrl(String shortCode) {
        Object cached = redisTemplate.opsForValue().get(urlKey + shortCode);
        return cached != null ? cached.toString() : null;
    }

    @Async
    @Override
    public void incrementViews(String shortCode) {
        redisTemplate.opsForValue().increment(urlViewsKey + shortCode);
    }

    @Override
    public void evictUrl(String shortCode) {
        redisTemplate.delete(urlKey + shortCode);
    }

    @Override
    public Set<String> getUrlViewsKeys(){
        Set keys = redisTemplate.keys(urlViewsKey + "*");
        return keys != null ? keys : new HashSet<>();
    }

    @Override
    public Long getAndResetViews(String shortCode) {
        String key = urlViewsKey + shortCode;
        Object views = redisTemplate.opsForValue().getAndDelete(key);
        return views != null ? Long.parseLong(views.toString()) : 0L;
    }

    @Override
    public void evictViews(String shortCode) {
        redisTemplate.delete(urlViewsKey + shortCode);
    }

    @Override
    public void incrementViewsAndResetTTL(String shortCode, Duration ttl) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] viewKey = (AppConstants.URL_VIEWS_KEY + shortCode).getBytes();
            byte[] urlKey = (AppConstants.URL_KEY + shortCode).getBytes();
            connection.stringCommands().incr(viewKey);
            connection.keyCommands().expire(urlKey, ttl.getSeconds());
            return null;
        });
    }
}