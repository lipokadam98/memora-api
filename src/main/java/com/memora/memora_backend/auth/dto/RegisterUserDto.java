package com.memora.memora_backend.auth.dto;

import lombok.Data;

@Data
public class RegisterUserDto {
    private String email;
    private String username;
    private String fullName;
    private String password;
}
