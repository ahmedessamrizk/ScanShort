package com.demo.utils;

import com.demo.entities.Url;

import java.time.LocalDateTime;

public record CachedUrl(
        String baseUrl,
        LocalDateTime expiresAt,
        String userId  // "guest" if no owner
) {
    public static CachedUrl from(Url url) {
        return new CachedUrl(
                url.getBaseUrl(),
                url.getExpiresAt(),
                url.getUser() != null ? url.getUser().getId().toString() : "guest"
        );
    }

    public static CachedUrl parse(String cached) {
        String[] parts = cached.split("\\|");
        return new CachedUrl(
                parts[0],
                LocalDateTime.parse(parts[1]),
                parts[2]
        );
    }

    public String serialize() {
        return baseUrl + "|" + expiresAt + "|" + userId;
    }

}