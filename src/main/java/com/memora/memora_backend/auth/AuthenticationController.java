package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.LoginResponse;
import com.memora.memora_backend.auth.dto.LoginUserDto;
import com.memora.memora_backend.auth.dto.RegisterUserDto;
import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.user.User;
import com.memora.memora_backend.user.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        UserDto user = UserDto.builder()
                .id(authenticatedUser.getId())
                .email(authenticatedUser.getEmail())
                .fullName(authenticatedUser.getFullName())
                .userName(authenticatedUser.getUsername())
                .build();

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .user(user)
                .expiresAt(new Date(System.currentTimeMillis() + jwtService.getExpirationTime()))
                .build();

        return ResponseEntity.ok(loginResponse);
    }
}