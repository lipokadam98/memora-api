package com.memora.memora_backend.auth.refreshtoken;

import com.memora.memora_backend.user.User;

public interface RefreshTokenService {
    String generateRefreshToken(User user);
    String generateRefreshToken(User user,String refreshToken);
    RefreshToken getRefreshTokenDetails(String refreshToken);
}
