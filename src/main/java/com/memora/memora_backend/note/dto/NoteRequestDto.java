package com.memora.memora_backend.note.dto;

import com.memora.memora_backend.user.dto.UserDto;
import lombok.Data;

@Data
public class NoteRequestDto {
    private String title;
    private String content;
    private UserDto user;
}
