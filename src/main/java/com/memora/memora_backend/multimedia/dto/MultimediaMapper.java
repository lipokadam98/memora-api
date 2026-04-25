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
        MultimediaResponseDto dto = new MultimediaResponseDto();
        dto.setId(multimedia.getId());
        dto.setContentUrl(storageService.generateSignedUrlForDownload(multimedia.getObjectKey()));
        dto.setThumbnailUrl(storageService.generateSignedUrlForDownload(multimedia.getThumbnailObjectKey()));
        dto.setContentType(multimedia.getContentType());
        dto.setObjectKey(multimedia.getObjectKey());
        dto.setUploadDate(Date.from(multimedia.getUploadDate()));
        return dto;
    }

    public MultimediaResponseDto toMultimediaResponseDtoWithSignedUrl(Multimedia multimedia) {
        MultimediaResponseDto dto = new MultimediaResponseDto();
        dto.setId(multimedia.getId());
        dto.setContentUrl("/multimedia/" + multimedia.getId() + "/content");
        dto.setThumbnailUrl("/multimedia/" + multimedia.getId() + "/thumbnail");
        dto.setContentType(multimedia.getContentType());
        dto.setObjectKey(multimedia.getObjectKey());
        dto.setUploadDate(Date.from(multimedia.getUploadDate()));
        dto.setSignedUrl(storageService.generateSignedUrlForUpload(multimedia));
        dto.setOriginalFileName(multimedia.getOriginalFileName());
        return dto;
    }

    public Multimedia toMultimediaFromDto(MultimediaRequestDto dto) {
        User user = new User(dto.getUser().getId());
        String objectKey = UUID.randomUUID() + "-" + dto.getOriginalFileName();
        var multimedia = new Multimedia();
        multimedia.setBucketName(bucketName);
        multimedia.setSize(dto.getSize());
        multimedia.setContentType(dto.getContentType());
        multimedia.setOriginalFileName(dto.getOriginalFileName());
        multimedia.setUser(user);
        multimedia.setThumbnailObjectKey(objectKey + "-thumbnail");
        multimedia.setObjectKey(objectKey);
        multimedia.setUploadDate(dto.getUploadDate().toInstant());
        return multimedia;
    }
}
