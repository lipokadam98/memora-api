package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.LoginResponse;
import com.memora.memora_backend.auth.dto.LoginUserDto;
import com.memora.memora_backend.auth.dto.RegisterUserDto;
import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.user.User;
import com.memora.memora_backend.user.UserRepository;
import com.memora.memora_backend.user.dto.Role;
import com.memora.memora_backend.user.dto.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Hashes inbound secret proofs and inserts a unique record identity into persistence mappings.
     */
    @Transactional
    @Override
    public User signup(RegisterUserDto input) {
        User user = User.builder()
                .fullName(input.getFullName())
                .email(input.getEmail())
                // TODO: abstract role delegation mechanics out of baseline defaults
                .role(Role.ADMIN)
                .enabled(true)
                .password(passwordEncoder.encode(input.getPassword()))
                .build();

        return userRepository.save(user);
    }

    /**
     * Validates input identities against hashed stores before minting state-independent access payloads.
     * @throws BadCredentialsException when validation criteria checks crash or fail verification.
     */
    @Override
    public LoginResponse authenticate(LoginUserDto input) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid credentials provided for authentication mapping verification request", e);
        }

        User authenticatedUser = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Resolved identity contextual state absent for identifier: " + input.getEmail()));

        UserDto userDto = UserDto.builder()
                .id(authenticatedUser.getId())
                .email(authenticatedUser.getEmail())
                .fullName(authenticatedUser.getFullName())
                .userName(authenticatedUser.getUsername())
                .build();

        String jwtToken = jwtService.generateToken(authenticatedUser);

        // Extract exact expiration bounds straight out of token metadata parameters rather than guessing via system clock mutations
        Date expirationDate = jwtService.extractExpiration(jwtToken);

        return LoginResponse.builder()
                .token(jwtToken)
                .user(userDto)
                .expiresAt(expirationDate)
                .build();
    }
}