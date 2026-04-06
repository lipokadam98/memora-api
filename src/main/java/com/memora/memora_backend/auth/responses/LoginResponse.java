package com.memora.memora_backend.auth.responses;

import com.memora.memora_backend.user.models.UserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private UserDto user;
    private long expiresIn;
}