package com.memora.memora_backend.note;

import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;

import java.util.List;

public interface NoteService {
    NoteResponseDto save(NoteRequestDto noteRequestDto);
    NoteResponseDto findById(Long id);
    void delete(Long id);
    void deleteAll(List<Long> ids);
    List<NoteResponseDto> findAll(Long userId);
    NoteResponseDto update(NoteRequestDto noteRequestDto);
}
