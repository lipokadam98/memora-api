package com.memora.memora_backend.notes;

import com.memora.memora_backend.notes.dto.NoteRequestDto;
import com.memora.memora_backend.notes.dto.NoteResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @GetMapping
    public List<NoteResponseDto> getAll(Long userId){
        return noteService.findAll(userId);
    }

    @GetMapping("/{id}")
    public NoteResponseDto getById(@PathVariable Long id){
        return noteService.findById(id);
    }

    @PostMapping
    public NoteResponseDto create(@RequestBody NoteRequestDto noteRequestDto){
        return noteService.save(noteRequestDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        noteService.delete(id);
    }
}
