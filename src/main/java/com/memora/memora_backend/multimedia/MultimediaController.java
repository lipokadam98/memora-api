package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.List;

@RestController
@RequestMapping(path = "/multimedia", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class MultimediaController {

    private final MultimediaService multimediaService;

    @GetMapping
    public List<MultimediaResponseDto> getAll(){
        return multimediaService.findAll();
    }

    @GetMapping("/{id}")
    public MultimediaResponseDto getById(@PathVariable Long id) {
        return multimediaService.findById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MultimediaResponseDto create(@RequestPart("file") MultipartFile file,
                                        @RequestPart("media") MultimediaRequestDto dto) {
        return multimediaService.save(dto,file);
    }

    @PutMapping("/{id}")
    public MultimediaResponseDto update(@PathVariable Long id,
                                        @RequestPart("file") MultipartFile file) {
        return multimediaService.update(id,file);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        multimediaService.delete(id);
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
