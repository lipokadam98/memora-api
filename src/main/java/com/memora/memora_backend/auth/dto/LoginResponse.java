package com.memora.memora_backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class LoginResponse {
    private String token;
    @JsonIgnore
    private String refreshToken;
    private UserDto user;
    private Date expiresAt;
}