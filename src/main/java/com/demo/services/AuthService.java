package com.demo.services;

import com.demo.dtos.request.LoginRequest;
import com.demo.dtos.request.SignupRequest;
import com.demo.dtos.response.LoginResponse;
import com.demo.dtos.response.UserDetailsResponse;
import jakarta.validation.Valid;

public interface AuthService {

    UserDetailsResponse createUser(@Valid SignupRequest request);

    LoginResponse login(@Valid LoginRequest request);
}
