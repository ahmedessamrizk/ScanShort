package com.demo.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ApiResponse", description = "Standard success response")
public class ApiResponse<T> {
    @Schema(description = "Success Flag", example="true")
    private boolean success;
    @Schema(description = "Response message", example = "Operation successful")
    private String message;
    @Schema(description = "Response data")
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
}