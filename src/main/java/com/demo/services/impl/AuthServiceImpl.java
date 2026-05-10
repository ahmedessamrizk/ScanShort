package com.demo.services.impl;

import com.demo.dtos.request.LoginRequest;
import com.demo.dtos.request.SignupRequest;
import com.demo.dtos.response.LoginResponse;
import com.demo.dtos.response.UserDetailsResponse;
import com.demo.entities.User;
import com.demo.exceptions.custom.ConflictException;
import com.demo.mappers.UserMapper;
import com.demo.repositories.UserRepository;
import com.demo.security.JwtService;
import com.demo.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserDetailsResponse createUser(SignupRequest request) {
        // check email is unique
        if (userRepository.existsByEmail(request.email().trim().toLowerCase()))
            throw new ConflictException("Email already exists");

        User user = userMapper.toEntity(request);
        user.setEmail(request.email().trim().toLowerCase());

        // encode password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        User createdUser = userRepository.save(user);

        // return data
        return userMapper.toDetailsDto(createdUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String token = jwtService.generateToken(authentication);
        Date expirationDate = jwtService.getExpirationDate();

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .expiresIn(expirationDate)
                .build();

        return response;
    }
}
