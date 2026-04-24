package com.memora.memora_backend.multimedia.dto;

import com.memora.memora_backend.user.dto.UserDto;
import lombok.Data;

import java.util.Date;

@Data
public class MultimediaRequestDto {
    private UserDto user;
    private Date uploadDate;
    private long size;
    private String contentType;
    private String originalFileName;
}
