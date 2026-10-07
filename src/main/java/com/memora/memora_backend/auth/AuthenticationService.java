package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.*;

public interface AuthenticationService {
    UserDto signup(RegisterUserDto input);
    LoginResponse authenticate(LoginUserDto input);
    LoginResponse reauthenticate(String refreshToken);
}