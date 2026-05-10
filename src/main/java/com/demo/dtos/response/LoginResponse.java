package com.demo.dtos.response;

import lombok.Builder;

import java.util.Date;

@Builder
public record LoginResponse(
        String token,
        Date expiresIn
) {}
