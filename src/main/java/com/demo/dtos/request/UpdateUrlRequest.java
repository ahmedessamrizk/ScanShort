package com.demo.dtos.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateUrlRequest(
        @Future(message = "Expiration time must be in the future")
        @NotNull(message = "Expiration time is required")
        LocalDateTime expiresAt
) {}
