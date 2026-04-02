package com.ecommerce.backend.serviceImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.dto.request.LoginRequest;
import com.ecommerce.backend.dto.request.RegisterRequest;
import com.ecommerce.backend.dto.response.AuthResponse;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.UnauthorizedException;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.service.AuthService;
import com.ecommerce.backend.util.Constant;

@Service
public class AuthServiceImpl implements AuthService{

private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
private final JwtUtil jwtUtil;

public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
}

// Register a new user
@Override

public AuthResponse register(RegisterRequest request) {
    // Edge Case: Email already exists
    String normalizedEmail = request.getEmail().toLowerCase();

    if (userRepository.findByEmail(normalizedEmail).isPresent()) {
        throw new BadRequestException(Constant.USER_ALREADY_EXISTS);
    }

    User user = new User();
    user.setName(request.getName());
    user.setEmail(normalizedEmail);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setAddress(request.getAddress());

    // Default Role
    user.setRole(Role.USER);
    userRepository.save(user);

    //Generate JWT token
    String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

    return AuthResponse.builder()
            .token(token)
            .name(user.getName())
            .email(user.getEmail())   
            .build();
}

//Login
@Override
public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new UnauthorizedException(Constant.INVALID_CREDENTIALS));


// Wrong Password
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new UnauthorizedException(Constant.INVALID_CREDENTIALS);
    } 

    // Generate JWT Token
    String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

    return AuthResponse.builder()
            .token(token)
            .name(user.getName())
            .email(user.getEmail())   
            .build();
}
}
