package com.demo.services.impl;

import com.demo.services.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean isAllowed(String key, int maxAttempts, int windowSeconds, int blockSeconds) {
        //You have max attempts through only window seconds and after that blocked for block seconds.

        //Key -> attempts:key
        String attemptsKey = "attempts:" + key;
        //Key -> block:key
        String blockKey = "block:" + key;

        //If this user is already blocked, return false
        if(Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))){
            return false;
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        //Need to start counting the windowSeconds from first attempt
        if(attempts == 1){
            redisTemplate.expire(attemptsKey, Duration.ofSeconds(windowSeconds));
        }

        //Block if user exceeds maxAttempts and delete the attempts.
        if(attempts > maxAttempts){
            redisTemplate.opsForValue().set(blockKey, "blocked", Duration.ofSeconds(blockSeconds));
            redisTemplate.delete(attemptsKey);
            return false;
        }

        return true;
    }
}
