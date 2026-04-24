package com.memora.memora_backend.multimedia.dto;

import lombok.Data;

@Data
public class ThumbnailCreationRequestDto {
    private Long id;
    private UploadStatus status;
}
