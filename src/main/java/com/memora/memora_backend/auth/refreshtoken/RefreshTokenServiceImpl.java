package com.memora.memora_backend.auth.refreshtoken;

import com.memora.memora_backend.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Slf4j
@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;

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
                .expirationDate(Instant.now().plus(REFRESH_TOKEN_VALIDITY))
                .build();

        var foundRefreshToken = refreshTokenRepository.findByUser(user);

        foundRefreshToken.ifPresent(refreshTokenRepository::delete);

        refreshTokenRepository.save(refreshToken);
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
}
