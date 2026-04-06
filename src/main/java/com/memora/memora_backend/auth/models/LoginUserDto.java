package com.memora.memora_backend.auth.models;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;
    private String password;
}
