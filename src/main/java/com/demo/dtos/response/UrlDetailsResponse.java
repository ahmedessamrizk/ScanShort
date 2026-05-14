package com.demo.dtos.response;

import com.demo.entities.enums.UrlStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UrlDetailsResponse(
     UUID id,
     String shortUrl,
     String baseUrl,
     Long numberOfViews,
     Instant expiresAt,
     Instant createdAt,
     UrlStatus status
) {}
