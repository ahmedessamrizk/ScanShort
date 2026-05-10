package com.demo.dtos.response;

public record UrlCreationResult(
    UrlDetailsResponse url,
    boolean isNew
) {}