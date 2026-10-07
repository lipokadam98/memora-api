package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.LoginResponse;
import com.memora.memora_backend.auth.dto.LoginUserDto;
import com.memora.memora_backend.auth.dto.RegisterUserDto;
import com.memora.memora_backend.auth.dto.UserDto;
import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.auth.refreshtoken.RefreshTokenService;
import com.memora.memora_backend.user.Role;
import com.memora.memora_backend.user.User;
import com.memora.memora_backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Hashes inbound secret proofs and inserts a unique record identity into persistence mappings.
     */
    @Transactional
    @Override
    public UserDto signup(RegisterUserDto input) {
        User user = User.builder()
                .fullName(input.getFullName())
                .email(input.getEmail())
                .role(Role.USER)
                .enabled(true)
                .password(passwordEncoder.encode(input.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User with email {} has been created", input.getEmail());
        return UserDto.builder()
                .userName(savedUser.getUsername())
                .email(savedUser.getEmail())
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .build();
    }

    /**
     * Validates input identities against hashed stores before minting state-independent access payloads.
     * @throws BadCredentialsException when validation criteria checks crash or fail verification.
     */
    @Override
    @Transactional
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

        String refreshToken = refreshTokenService.generateRefreshToken(authenticatedUser);

        return createLoginresponse(authenticatedUser,refreshToken);
    }

    @Override
    @Transactional
    public LoginResponse reauthenticate(String oldRefreshToken) {
        var tokenDetails = refreshTokenService.getRefreshTokenDetails(oldRefreshToken);
        var user = userRepository.findById(tokenDetails.getUser().getId()).orElse(null);

        if(user == null){
            throw new EntityNotFoundException("User not found during token refresh");
        }

        String refreshToken = refreshTokenService.generateRefreshToken(user, oldRefreshToken);

        return createLoginresponse(user,refreshToken);
    }

    private LoginResponse createLoginresponse(User user, String refreshToken){
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userName(user.getUsername())
                .build();

        String jwtToken = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .expiresAt(jwtService.extractExpiration(jwtToken))
                .build();
    }
}