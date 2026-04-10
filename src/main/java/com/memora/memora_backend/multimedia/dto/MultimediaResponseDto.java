package com.memora.memora_backend.multimedia.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MultimediaResponseDto {
    private Long id;
    private String contentUrl;
    private String thumbnailUrl;
    private String contentType;
    private String objectKey;
    private Date uploadDate;
}
