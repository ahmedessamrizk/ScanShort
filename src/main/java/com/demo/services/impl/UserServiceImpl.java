package com.demo.services.impl;

import com.demo.dtos.request.UpdateProfileRequest;
import com.demo.dtos.response.UserDetailsResponse;
import com.demo.entities.User;
import com.demo.exceptions.custom.NotFoundException;
import com.demo.mappers.UserMapper;
import com.demo.repositories.UserRepository;
import com.demo.services.UserService;
import com.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    private User getUserOrThrow(UUID id){
        return this.userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with this id: " + id));
    }

    @Override
    public UserDetailsResponse getProfile() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = getUserOrThrow(currentUserId);
        return userMapper.toDetailsDto(currentUser);
    }

    @Override
    @Transactional
    public UserDetailsResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = getUserOrThrow(SecurityUtils.getCurrentUserId());
        currentUser.setName(request.name());
        User updatedUser = this.userRepository.save(currentUser);
        return userMapper.toDetailsDto(updatedUser);
    }
}
