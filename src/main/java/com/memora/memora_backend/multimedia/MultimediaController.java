package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.List;

//TODO Return and accept only DTO to minimize data and to make it easier to use
@RestController
@RequestMapping(path = "/multimedia", produces = MediaType.APPLICATION_JSON_VALUE)
public class MultimediaController {

    private final MultimediaService multimediaService;

    public MultimediaController(MultimediaService multimediaService) {
        this.multimediaService = multimediaService;
    }

    @GetMapping
    public List<MultimediaResponseDto> getAll(){
        return multimediaService.findAll();
    }

    @GetMapping("/{id}")
    public Multimedia getById(@PathVariable Long id) {
        return multimediaService.findById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Multimedia create(@RequestPart("file") MultipartFile file,
                             @RequestPart("media") MultimediaRequestDto dto) {
        return multimediaService.save(dto,file);
    }

    @PutMapping
    public Multimedia update(@RequestBody Multimedia multimedia) {
        return multimediaService.update(multimedia);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        multimediaService.delete(id);
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) {
        var resource = multimediaService.getThumbnail(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
