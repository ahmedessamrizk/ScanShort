package com.demo.controllers;

import com.demo.dtos.request.LoginRequest;
import com.demo.dtos.request.SignupRequest;
import com.demo.dtos.response.LoginResponse;
import com.demo.dtos.response.UserDetailsResponse;
import com.demo.services.AuthService;
import com.demo.utils.ApiResponse;
import com.demo.annotations.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for signup and login")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Sign up", description = "Register a new user account")@ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", ref = "#/components/responses/400"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", ref = "#/components/responses/409"),
    })
    @PostMapping("/signup")
    @RateLimit(maxAttempts = 5, windowSeconds = 60, blockSeconds = 300)
    public ResponseEntity<ApiResponse<UserDetailsResponse>> createUser(@Valid @RequestBody SignupRequest request){
        UserDetailsResponse user = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User is created successfully", user));
    }

    @Operation(summary = "Login", description = "Authenticate with email and password and receive JWT token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials", ref = "#/components/responses/401"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many login attempts", ref = "#/components/responses/429")
    })
    @PostMapping("/login")
    @RateLimit(maxAttempts = 3, windowSeconds = 60, blockSeconds = 300)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request){
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }


}
