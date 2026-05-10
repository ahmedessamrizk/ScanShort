package com.demo.dtos.response;

import com.demo.entities.enums.UrlStatus;

import java.util.UUID;

public record UrlListResponse(
        UUID id,
        String baseUrl,
        String shortUrl,
        Long numberOfViews,
        UrlStatus status
) {}
