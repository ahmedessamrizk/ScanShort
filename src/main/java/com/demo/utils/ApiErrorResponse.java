package com.demo.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(name = "ApiErrorResponse", description = "Standard error response")
public class ApiErrorResponse {
    @Schema(description = "Success Flag", example="false")
    private boolean success;
    @Schema(description = "Error message", example = "Resource not found")
    private String message;
    @Schema(description = "Error code", example = "NOT_FOUND")
    private String errorCode;
    @Schema(description = "Field errors", nullable = true)
    private List<FieldError> errors;

    public static ApiErrorResponse of(String message, String errorCode) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    public static ApiErrorResponse of(String message, String errorCode, List<FieldError> errors) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .build();
    }
}
