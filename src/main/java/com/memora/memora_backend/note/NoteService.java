package com.memora.memora_backend.note;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;

import java.util.List;

public interface NoteService {
    NoteResponseDto save(NoteRequestDto noteRequestDto);
    NoteResponseDto findById(Long id);
    void delete(Long id);
    void deleteAll(List<Long> ids);
    CursorPage<NoteResponseDto> findAll(Long userId, String cursor, int limit);
    NoteResponseDto update(NoteRequestDto noteRequestDto);
}
