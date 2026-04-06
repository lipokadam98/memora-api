package com.memora.memora_backend.auth.models;

import lombok.Data;

@Data
public class RegisterUserDto {
    private String email;
    private String username;
    private String fullName;
    private String password;
}
