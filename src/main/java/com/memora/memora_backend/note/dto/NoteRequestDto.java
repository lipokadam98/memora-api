package com.memora.memora_backend.note.dto;

import lombok.Data;

@Data
public class NoteRequestDto {
    private String title;
    private String content;
    private Long userId;
}
