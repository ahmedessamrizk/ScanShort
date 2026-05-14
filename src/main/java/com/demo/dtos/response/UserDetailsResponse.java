package com.demo.dtos.response;

import java.time.Instant;
import java.util.UUID;

public record UserDetailsResponse (
    UUID id,
    String name,
    String email,
    Instant createdAt,
    Instant updatedAt
){}
