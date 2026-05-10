package com.demo.controllers;

import com.demo.annotations.RateLimit;
import com.demo.services.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "URL", description = "Endpoints for creating and managing shortened URLs")
public class RedirectController {
    private final UrlService urlService;

    @Operation(summary = "Redirect to original URL", description = "Redirects to the original URL based on short code. Increments view count on every hit")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
            @ApiResponse(responseCode = "403", description = "URL is disabled", ref = "#/components/responses/403"),
            @ApiResponse(responseCode = "404", description = "URL not found", ref = "#/components/responses/404"),
            @ApiResponse(responseCode = "410", description = "URL has expired", ref = "#/components/responses/410"),
            @ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")
    })
    @GetMapping("/{shortCode}")
    @RateLimit(maxAttempts = 30, windowSeconds = 60, blockSeconds = 120)
    public ResponseEntity<Void> redirectUrl(@PathVariable String shortCode){
        String baseUrl = urlService.redirectUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(baseUrl)).build();
    }

}
