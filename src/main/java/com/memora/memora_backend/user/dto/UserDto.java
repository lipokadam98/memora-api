package com.memora.memora_backend.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String email;
    private String userName;
    private String fullName;
}
