package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.multimedia.dto.ThumbnailCreationRequestDto;

import java.util.List;

public interface MultimediaService {
    List<MultimediaResponseDto> save(List<MultimediaRequestDto> multimediaRequestDto);
    MultimediaResponseDto findById(Long id);
    CursorPage<MultimediaResponseDto> findAll(Long userId, String cursor, int limit);
    List<MultimediaResponseDto> createThumbnails(List<ThumbnailCreationRequestDto> thumbnailCreationRequestDtoList);
    void delete(Long id);
    void deleteAll(List<Long> ids);
}
