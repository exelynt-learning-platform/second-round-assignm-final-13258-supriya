package com.ecommerce.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.backend.dto.request.LoginRequest;
import com.ecommerce.backend.dto.request.RegisterRequest;
import com.ecommerce.backend.dto.response.AuthResponse;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Role;
import com.ecommerce.backend.exception.BadRequestException;
import com.ecommerce.backend.exception.UnauthorizedException;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.serviceImpl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test")
                .email("test@test.com")
                .password("123456")
                .address("Pune")
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(any()))
                .thenReturn("encoded");
        when(jwtUtil.generateToken(any(), any()))
                .thenReturn("token");

        AuthResponse response = authService.register(request);

        assertNotNull(response.getToken());
        assertEquals("Test", response.getName());
        assertEquals("test@test.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test")
                .email("test@test.com")
                .password("123456")
                .address("Pune")
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_caseInsensitive_throwsBadRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test")
                .email("Test@Test.com")
                .password("123456")
                .address("Pune")
                .build();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        User user = User.builder()
                .name("Test")
                .email("test@test.com")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded"))
                .thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", "USER"))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("test@test.com", "123456"));

        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Test", response.getName());
    }

    @Test
    void login_invalidPassword_throwsUnauthorized() {
        User user = User.builder()
                .email("test@test.com")
                .password("encoded")
                .build();

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("test@test.com", "wrong")));
    }

    @Test
    void login_userNotFound_throwsUnauthorized() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("unknown@test.com", "123456")));
    }
}
