package com.memora.memora_backend.multimedia.dto;

import com.memora.memora_backend.multimedia.Multimedia;
import com.memora.memora_backend.storage.StorageService;
import com.memora.memora_backend.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class MultimediaMapper {

    @Value("${cloud.bucketName}")
    private String bucketName;

    private final StorageService storageService;

    public MultimediaMapper(StorageService storageService) {
        this.storageService = storageService;
    }

    public MultimediaResponseDto toMultimediaResponseDto(Multimedia multimedia) {
        return MultimediaResponseDto.builder()
                .id(multimedia.getId())
                .contentUrl(storageService.generateSignedUrlForDownload(multimedia.getObjectKey()))
                .thumbnailUrl(storageService.generateSignedUrlForDownload(multimedia.getThumbnailObjectKey()))
                .contentType(multimedia.getContentType())
                .objectKey(multimedia.getObjectKey())
                .uploadDate(Date.from(multimedia.getUploadDate()))
                .build();
    }

    public MultimediaResponseDto toMultimediaResponseDtoWithSignedUrl(Multimedia multimedia) {
        return MultimediaResponseDto.builder()
                .id(multimedia.getId())
                .contentType(multimedia.getContentType())
                .objectKey(multimedia.getObjectKey())
                .uploadDate(Date.from(multimedia.getUploadDate()))
                .signedUrl(storageService.generateSignedUrlForUpload(multimedia))
                .originalFileName(multimedia.getOriginalFileName())
                .build();
    }

    public Multimedia toMultimediaFromDto(MultimediaRequestDto dto,User user) {
        String objectKey = UUID.randomUUID() + "-" + dto.getOriginalFileName();
        return Multimedia.builder()
                .bucketName(bucketName)
                .size(dto.getSize())
                .contentType(dto.getContentType())
                .originalFileName(dto.getOriginalFileName())
                .user(user)
                .thumbnailObjectKey(objectKey + "-thumbnail")
                .objectKey(objectKey)
                .uploadDate(dto.getUploadDate().toInstant())
                .build();
    }
}
