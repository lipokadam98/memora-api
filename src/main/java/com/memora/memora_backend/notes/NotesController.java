package com.memora.memora_backend.notes;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class NotesController {
    private final NotesService notesService;

    @GetMapping
    public List<Notes> getAll(Long userId){
        return notesService.findAll(userId);
    }

    @GetMapping("/{id}")
    public Notes getById(@PathVariable Long id){
        return notesService.findById(id);
    }

    @PostMapping
    public Notes create(@RequestBody Notes notes){
        return notesService.save(notes);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        notesService.delete(id);
    }
}
