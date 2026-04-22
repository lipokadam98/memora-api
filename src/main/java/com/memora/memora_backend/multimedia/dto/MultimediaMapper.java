package com.memora.memora_backend.multimedia.dto;

import com.memora.memora_backend.multimedia.Multimedia;
import com.memora.memora_backend.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Component
public class MultimediaMapper {

    @Value("${cloud.bucketName}")
    private String bucketName;

    public MultimediaResponseDto toMultimediaResponseDto(Multimedia multimedia) {
        MultimediaResponseDto dto = new MultimediaResponseDto();
        dto.setId(multimedia.getId());
        dto.setContentUrl("/multimedia/" + multimedia.getId() + "/content");
        dto.setThumbnailUrl("/multimedia/" + multimedia.getId() + "/thumbnail");
        dto.setContentType(multimedia.getContentType());
        dto.setObjectKey(multimedia.getObjectKey());
        dto.setUploadDate(Date.from(multimedia.getUploadDate()));
        return dto;
    }

    public Multimedia toMultimedia(MultimediaRequestDto dto, MultipartFile file) {
        User user = new User(dto.getUser().getId());
        String objectKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
        var multimedia = new Multimedia();
        multimedia.setBucketName(bucketName);
        multimedia.setSize(file.getSize());
        multimedia.setContentType(file.getContentType());
        multimedia.setOriginalFileName(file.getOriginalFilename());
        multimedia.setUser(user);
        multimedia.setThumbnailObjectKey(objectKey + "-thumbnail");
        multimedia.setObjectKey(objectKey);
        multimedia.setUploadDate(dto.getUploadDate().toInstant());
        return multimedia;
    }
}
