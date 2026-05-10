package com.demo.utils;

import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@NoArgsConstructor
public final class DateTimeUtils {
    public static Duration toDuration(LocalDateTime expiresAt){
        return Duration.between(LocalDateTime.now(), expiresAt);
    }

    public static LocalDateTime toLocalDateTime(Duration duration){
        return LocalDateTime.now().plusSeconds(duration.getSeconds());
    }

    public static Duration getOptimalCacheTtl(LocalDateTime expiresAt) {
        Duration remainingExpiry = Duration.between(LocalDateTime.now(), expiresAt);
        Duration maxCacheTtl = AppConstants.URL_CACHE_EXPIRATION;

        // use whichever is shorter
        return remainingExpiry.compareTo(maxCacheTtl) < 0 ? remainingExpiry : maxCacheTtl;
    }
}
