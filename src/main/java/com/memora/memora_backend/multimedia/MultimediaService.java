package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.List;

public interface MultimediaService {
    Multimedia save(MultimediaRequestDto multimediaRequestDto, MultipartFile file);
    Multimedia findById(Long id);
    void delete(Long id);
    Multimedia update(Multimedia multimedia);
    List<MultimediaResponseDto> findAll();
    Resource getThumbnail(Long id);
}
