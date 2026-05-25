package com.memora.memora_backend.notes.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NoteResponseDto {
    private Long id;
    private String title;
    private String content;
    private Date createdAt;
    private Date updatedAt;
}
