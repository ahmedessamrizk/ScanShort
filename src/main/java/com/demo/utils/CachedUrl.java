package com.demo.utils;

import com.demo.entities.Url;

import java.time.Instant;

public record CachedUrl(
        String baseUrl,
        Instant expiresAt,
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
                Instant.parse(parts[1]),
                parts[2]
        );
    }

    public String serialize() {
        return baseUrl + "|" + expiresAt + "|" + userId;
    }

}