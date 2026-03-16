package com.memora.memora_backend.multimedia;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/multimedia")
public class MultimediaController {

    private final MultimediaService multimediaService;

    public MultimediaController(MultimediaService multimediaService) {
        this.multimediaService = multimediaService;
    }

    @GetMapping
    public List<Multimedia> getAll(){
        return multimediaService.findAll();
    }

    @GetMapping("/{id}")
    public Multimedia getById(@PathVariable Long id) {
        return multimediaService.findById(id);
    }

    @PostMapping
    public Multimedia create(@RequestBody Multimedia media) {
        return multimediaService.save(media);
    }

    @PutMapping("/{id}")
    public Multimedia update(@RequestBody Multimedia multimedia) {
        return multimediaService.update(multimedia);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        multimediaService.delete(id);
    }
}
