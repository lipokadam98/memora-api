package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.multimedia.dto.ThumbnailCreationRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/multimedia", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public void delete(@PathVariable Long id) {
        multimediaService.delete(id);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids) {
        multimediaService.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) {
        var resource = multimediaService.downloadThumbnail(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> getContent(@PathVariable Long id) {
        var multimedia = multimediaService.findById(id);
        var resource = multimediaService.downloadContent(multimedia.getObjectKey());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(multimedia.getContentType()))
                .body(resource);
    }
}
