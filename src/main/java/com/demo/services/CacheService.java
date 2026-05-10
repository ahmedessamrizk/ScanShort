package com.demo.services;

import java.time.Duration;
import java.util.Set;

public interface CacheService {
    void cacheUrl(String shortCode, String baseUrl, Duration ttl);
    String getCachedUrl(String shortCode);
    void incrementViews(String shortCode);
    void evictUrl(String shortCode);
    Set<String> getUrlViewsKeys();
    Long getAndResetViews(String shortCode);

    void evictViews(String shortCode);

    void incrementViewsAndResetTTL(String shortCode, Duration ttl);
}
