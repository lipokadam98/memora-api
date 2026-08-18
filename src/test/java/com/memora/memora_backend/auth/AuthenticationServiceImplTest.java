package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.LoginResponse;
import com.memora.memora_backend.auth.dto.LoginUserDto;
import com.memora.memora_backend.auth.dto.RegisterUserDto;
import com.memora.memora_backend.auth.dto.UserDto;
import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.user.Role;
import com.memora.memora_backend.user.User;
import com.memora.memora_backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User mockUser;
    private RegisterUserDto registerUserDto;
    private LoginUserDto loginUserDto;

    @BeforeEach
    void setUp() {
        registerUserDto = new RegisterUserDto();
        registerUserDto.setFullName("John Doe");
        registerUserDto.setEmail("john.doe@example.com");
        registerUserDto.setPassword("plainPassword");

        loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("john.doe@example.com");
        loginUserDto.setPassword("plainPassword");

        mockUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .role(Role.USER)
                .enabled(true)
                .password("encodedPassword")
                .build();
    }

    @Test
    @DisplayName("signup() - Should encode password, persist user, and return UserDto")
    void testSignup_Success() {
        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserDto result = authenticationService.signup(registerUserDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("John Doe", result.getFullName());

        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("authenticate() - Should authenticate and return LoginResponse with JWT details")
    void testAuthenticate_Success() {
        String token = "mocked.jwt.token";
        Date expirationDate = new Date(System.currentTimeMillis() + 3600000);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(loginUserDto.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn(token);
        when(jwtService.extractExpiration(token)).thenReturn(expirationDate);

        LoginResponse response = authenticationService.authenticate(loginUserDto);

        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(expirationDate, response.getExpiresAt());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("john.doe@example.com", response.getUser().getEmail());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(loginUserDto.getEmail());
        verify(jwtService, times(1)).generateToken(mockUser);
        verify(jwtService, times(1)).extractExpiration(token);
    }

    @Test
    @DisplayName("authenticate() - Should throw BadCredentialsException when authentication manager fails")
    void testAuthenticate_BadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticate(loginUserDto)
        );

        assertTrue(exception.getMessage().contains("Invalid credentials provided for authentication mapping verification request"));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("authenticate() - Should throw EntityNotFoundException when user is missing after authentication")
    void testAuthenticate_UserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(loginUserDto.getEmail())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> authenticationService.authenticate(loginUserDto)
        );

        assertTrue(exception.getMessage().contains("Resolved identity contextual state absent for identifier"));
        verify(jwtService, never()).generateToken(any());
    }
}