package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.List;

public interface MultimediaService {
    MultimediaResponseDto save(MultimediaRequestDto multimediaRequestDto, MultipartFile file);
    MultimediaResponseDto findById(Long id);
    void delete(Long id);
    MultimediaResponseDto update(Long id, MultipartFile file, MultimediaRequestDto multimedia);
    List<MultimediaResponseDto> findAll();
    Resource downloadMultimedia(Long id);
}
