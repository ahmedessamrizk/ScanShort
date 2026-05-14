package com.demo.utils;

import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@NoArgsConstructor
public final class DateTimeUtils {
    public static Duration toDuration(Instant expiresAt){
        return Duration.between(Instant.now(), expiresAt);
    }

    public static Instant toInstant(Duration duration){
        return Instant.now().plusSeconds(duration.getSeconds());
    }

    public static Duration getOptimalCacheTtl(Instant expiresAt) {
        Duration remainingExpiry = Duration.between(Instant.now(), expiresAt);
        Duration maxCacheTtl = AppConstants.URL_CACHE_EXPIRATION;

        // use whichever is shorter
        return remainingExpiry.compareTo(maxCacheTtl) < 0 ? remainingExpiry : maxCacheTtl;
    }
}
