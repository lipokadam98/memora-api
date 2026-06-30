package com.memora.memora_backend.multimedia.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MultimediaRequestDto {
    private Long userId;
    private Date uploadDate;
    private long size;
    private String contentType;
    private String originalFileName;
}
