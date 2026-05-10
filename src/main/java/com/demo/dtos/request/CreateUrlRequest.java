package com.demo.dtos.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record CreateUrlRequest(
  @NotBlank(message = "Base URL is required")
  @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Base URL must be a valid URL")
  String baseUrl,

  @Future(message = "Expiration time must be in the future")
  LocalDateTime expiresAt,

  Boolean allowDuplicate,

  @Pattern(regexp = "^[a-zA-Z0-9_-]{3,15}$", message = "Custom code must be 3-15 characters, alphanumeric, dashes and underscores only")
  String customCode


) {}
