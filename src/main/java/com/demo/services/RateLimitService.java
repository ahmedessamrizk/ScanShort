package com.demo.services;

public interface RateLimitService {
    boolean isAllowed(String key, int maxAttempts, int windowSeconds, int blockSeconds);
}
