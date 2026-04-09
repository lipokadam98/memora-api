package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.List;

public interface MultimediaService {
    List<MultimediaResponseDto> save(MultimediaRequestDto multimediaRequestDto, MultipartFile[] files);
    MultimediaResponseDto findById(Long id);
    void delete(Long id);
    MultimediaResponseDto update(Long id, MultipartFile file);
    List<MultimediaResponseDto> findAll();
    Resource downloadThumbnail(Long id);
    Resource downloadContent(String objectKey);
}
