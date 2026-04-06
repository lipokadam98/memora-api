package com.memora.memora_backend.auth.dto;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;
    private String password;
}
