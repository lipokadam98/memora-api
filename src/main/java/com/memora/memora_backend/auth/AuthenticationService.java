package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.LoginUserDto;
import com.memora.memora_backend.auth.dto.RegisterUserDto;
import com.memora.memora_backend.user.User;

public interface AuthenticationService {
    User signup(RegisterUserDto input);
    User authenticate(LoginUserDto input);
}