package com.memora.memora_backend.auth.dto;

import com.memora.memora_backend.user.dto.UserDto;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class LoginResponse {
    private String token;
    private UserDto user;
    private Date expiresAt;
}