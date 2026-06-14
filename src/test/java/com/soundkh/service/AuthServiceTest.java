package com.soundkh.service;

import com.soundkh.dto.AuthDto;
import com.soundkh.entity.User;
import com.soundkh.repository.UserRepository;
import com.soundkh.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authManager;
    @InjectMocks AuthService authService;

    @Test
    void register_success() {
        when(userRepository.existsByUsername("chhay")).thenReturn(false);
        when(userRepository.existsByEmail("chhay@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generate(anyString(), anyString())).thenReturn("token123");

        var res = authService.register(new AuthDto.RegisterRequest("chhay", "chhay@test.com", "pass123"));

        assertThat(res.token()).isEqualTo("token123");
        assertThat(res.username()).isEqualTo("chhay");
        assertThat(res.role()).isEqualTo("LISTENER");
    }

    @Test
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("chhay")).thenReturn(true);
        assertThatThrownBy(() ->
            authService.register(new AuthDto.RegisterRequest("chhay", "x@x.com", "pass123"))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Username already taken");
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByUsername("chhay")).thenReturn(false);
        when(userRepository.existsByEmail("chhay@test.com")).thenReturn(true);
        assertThatThrownBy(() ->
            authService.register(new AuthDto.RegisterRequest("chhay", "chhay@test.com", "pass123"))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Email already registered");
    }

    @Test
    void login_success() {
        var user = new User();
        user.setUsername("chhay");
        user.setPasswordHash("hashed");
        when(userRepository.findByUsername("chhay")).thenReturn(Optional.of(user));
        when(jwtUtil.generate("chhay", "LISTENER")).thenReturn("token123");

        var res = authService.login(new AuthDto.LoginRequest("chhay", "pass123"));

        assertThat(res.token()).isEqualTo("token123");
        verify(authManager).authenticate(any());
    }

    @Test
    void login_badCredentials_throws() {
        doThrow(new BadCredentialsException("bad")).when(authManager).authenticate(any());
        assertThatThrownBy(() ->
            authService.login(new AuthDto.LoginRequest("chhay", "wrong"))
        ).isInstanceOf(BadCredentialsException.class);
    }
}
