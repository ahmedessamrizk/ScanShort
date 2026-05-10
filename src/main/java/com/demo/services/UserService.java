package com.demo.services;

import com.demo.dtos.request.UpdateProfileRequest;
import com.demo.dtos.response.UserDetailsResponse;

public interface UserService {
    UserDetailsResponse getProfile();

    UserDetailsResponse updateProfile(UpdateProfileRequest request);
}
