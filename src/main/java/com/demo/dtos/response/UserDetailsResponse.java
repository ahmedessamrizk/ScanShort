package com.demo.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailsResponse (
    UUID id,
    String name,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
){}
