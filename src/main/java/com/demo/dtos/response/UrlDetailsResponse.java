package com.demo.dtos.response;

import com.demo.entities.enums.UrlStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UrlDetailsResponse(
     UUID id,
     String shortUrl,
     String baseUrl,
     Long numberOfViews,
     LocalDateTime expiresAt,
     LocalDateTime createdAt,
     UrlStatus status
) {}
