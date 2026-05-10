package com.demo.controllers;

import com.demo.annotations.RateLimit;
import com.demo.dtos.request.UpdateProfileRequest;
import com.demo.dtos.response.UserDetailsResponse;
import com.demo.services.UserService;
import com.demo.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "Endpoints for managing user profile")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get profile", description = "Get current authenticated user profile")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", ref = "#/components/responses/403")
    })
    @GetMapping("/profile")
    @RateLimit(maxAttempts = 20, windowSeconds = 60, blockSeconds = 300)
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getProfile(){
        UserDetailsResponse response = this.userService.getProfile();
        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", response));
    }

    @Operation(summary = "Update profile", description = "Update current authenticated user name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile is updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", ref = "#/components/responses/400"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", ref = "#/components/responses/403")
    })
    @PatchMapping("/profile")
    @RateLimit(maxAttempts = 10, windowSeconds = 60, blockSeconds = 300)
    public ResponseEntity<ApiResponse<UserDetailsResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest updateProfileRequest){
        UserDetailsResponse response = this.userService.updateProfile(updateProfileRequest);
        return ResponseEntity.ok(ApiResponse.success("User profile is updated successfully", response));
    }

}
