package com.memora.memora_backend.multimedia.dto;

import lombok.Data;

@Data
public class MultimediaResponseDto {
    private Long id;
    private String contentUrl;
    private String thumbnailUrl;
}
