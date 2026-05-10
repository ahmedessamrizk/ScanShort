package com.demo.controllers;

import com.demo.annotations.RateLimit;
import com.demo.dtos.request.CreateUrlRequest;
import com.demo.dtos.request.UpdateUrlRequest;
import com.demo.dtos.response.UrlCreationResult;
import com.demo.dtos.response.UrlDetailsResponse;
import com.demo.dtos.response.UrlListResponse;
import com.demo.entities.enums.UrlStatus;
import com.demo.services.UrlService;
import com.demo.utils.ApiResponse;
import com.demo.utils.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/urls")
@RateLimit(maxAttempts = 20, windowSeconds = 60, blockSeconds = 300)
@Tag(name = "URL", description = "Endpoints for creating and managing shortened URLs")
public class UrlController {
    private final UrlService urlService;

    @Operation(
            summary = "Create shortened URL",
            description = "Creates a new shortened URL. Guest users get auto-generated codes with 30 day expiry. Authenticated users can set custom codes, expiry dates, and allow duplicates"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "URL created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL already shortened before"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", ref = "#/components/responses/400"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Custom code already taken or reserved", ref = "#/components/responses/409"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")
    })
    @PostMapping
    @RateLimit(maxAttempts = 10, windowSeconds = 60, blockSeconds = 300)
    public ResponseEntity<ApiResponse<UrlDetailsResponse>> createUrl(@Valid @RequestBody CreateUrlRequest request){
        UrlCreationResult response = this.urlService.createUrl(request);
        return  !response.isNew()?
                ResponseEntity.ok(ApiResponse.success("Url is shortened before", response.url())) :
                ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Url is created successfully", response.url()));
    }

    @Operation(summary = "Get URL details", description = "Get full details of a shortened URL owned by current user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Url is fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found", ref = "#/components/responses/404"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", ref = "#/components/responses/403"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")

    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UrlDetailsResponse>> getUrl(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.success("Url is fetched successfully", urlService.getUrl(id)));
    }

    @Operation(summary = "List URLs", description = "List all shortened URLs for current user with optional status filter and pagination")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URLs fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", ref = "#/components/responses/403"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<UrlListResponse>>> getUrls(
            @RequestParam(required = false) UrlStatus status,
            @RequestParam(defaultValue = "1") @Positive Integer page,
            @RequestParam(defaultValue = "10") @Positive Integer size
    ){
        PaginatedResponse<UrlListResponse> response = urlService.getUrls(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Urls are fetched successfully", response));
    }

    @Operation(summary = "Disable URL", description = "Disable an active shortened URL owned by current user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "URL disabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "URL is not active", ref = "#/components/responses/400"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found", ref = "#/components/responses/404"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")
    })
    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableUrl(@PathVariable UUID id){
        urlService.disableUrl(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Enable URL", description = "Re-enable a disabled shortened URL owned by current user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL enabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "URL is not disabled or is expired", ref = "#/components/responses/400"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found", ref = "#/components/responses/404"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")
    })
    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUrl(@PathVariable UUID id){
        urlService.enableUrl(id);
        return ResponseEntity.ok(ApiResponse.success("Url is activated successfully", null));
    }

    @Operation(summary = "Update expiration date", description = "Update the expiration date of a shortened URL owned by current user. Also reactivates expired URLs")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expiration date updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error — date must be in future", ref = "#/components/responses/400"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found", ref = "#/components/responses/404"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")
    })
    @PatchMapping("/{id}/expiration")
    public ResponseEntity<ApiResponse<UrlDetailsResponse>> expireUrl(@PathVariable UUID id, @Valid @RequestBody UpdateUrlRequest request){
        UrlDetailsResponse updatedUrl =  urlService.expireUrl(id, request);
        return ResponseEntity.ok(ApiResponse.success("Expiration date is updated successfully", updatedUrl));
    }

    @Operation(summary = "Generate QR code", description = "Generates a QR code PNG image for any valid short URL. Public endpoint — no authentication required")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "QR code generated successfully — returns PNG image"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Short URL not found", ref = "#/components/responses/404"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests", ref = "#/components/responses/429")
    })
    @GetMapping("/{shortCode}/qr")
    @RateLimit(maxAttempts = 20, windowSeconds = 60, blockSeconds = 300)
    public ResponseEntity<byte[]> generateQr(@PathVariable String shortCode) {
        byte[] qrImage = urlService.generateQr(shortCode);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qr-" + shortCode + ".png\"")
                .body(qrImage);
    }
}
