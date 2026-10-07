package com.memora.memora_backend.auth.refreshtoken;

import com.memora.memora_backend.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${refresh.token.expiration-days}")
    private Long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * During login, we generate a refresh token for the user that we persist in the database
     * this is used to generate a new access token that will be used to authenticate the user
     * this is necessary to store because we need to be able to generate a new access token when the current one expires
     * @param user User to generate refresh token for
     * @return refresh token
     */
    @Override
    @Transactional
    public String generateRefreshToken(User user) {
        var token = UUID.randomUUID().toString();
        var refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expirationDate(Instant.now().plus(Duration.ofDays(refreshTokenExpiration)))
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    public String generateRefreshToken(User user, String refreshToken) {
        var token = UUID.randomUUID().toString();

        var newRefreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expirationDate(Instant.now().plus(Duration.ofDays(refreshTokenExpiration)))
                .build();

        var foundRefreshToken = refreshTokenRepository.findByToken(refreshToken);

        foundRefreshToken.ifPresent(refreshTokenRepository::delete);

        refreshTokenRepository.save(newRefreshToken);
        return token;
    }

    @Override
    public RefreshToken getRefreshTokenDetails(String refreshToken) {
        var token = refreshTokenRepository.findByToken(refreshToken).orElse(null);

        if(token == null){
            throw new IllegalArgumentException("Refresh token not found");
        }

        if(token.getExpirationDate().isBefore(Instant.now())){
            throw new IllegalArgumentException("Refresh token expired");
        }

        return token;
    }

    @Override
    public String createRefreshTokenCookie(String refreshToken){
        var responseCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/reauthenticate")
                .maxAge(Duration.ofDays(refreshTokenExpiration))
                .sameSite("Strict")
                .build();

        return responseCookie.toString();
    }
}
