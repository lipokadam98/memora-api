package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.multimedia.dto.ThumbnailCreationRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/multimedia", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class MultimediaController {

    private final MultimediaService multimediaService;

    @GetMapping
    public CursorPage<MultimediaResponseDto> getAll(
            @RequestParam Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return multimediaService.findAll(userId,cursor, limit);
    }

    @GetMapping("/{id}")
    public MultimediaResponseDto getById(@PathVariable Long id) {
        return multimediaService.findById(id);
    }

    @PostMapping
    public List<MultimediaResponseDto> create(@RequestBody List<MultimediaRequestDto> multimediaRequestDtoList) {
        return multimediaService.save(multimediaRequestDtoList);
    }

    @PostMapping("/create-thumbnails")
    public List<MultimediaResponseDto> createThumbnails(@RequestBody List<ThumbnailCreationRequestDto> thumbnailCreationRequestDtoList) {
        return multimediaService.createThumbnails(thumbnailCreationRequestDtoList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        multimediaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids) {
        multimediaService.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }
}
