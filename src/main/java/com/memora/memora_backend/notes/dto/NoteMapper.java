package com.memora.memora_backend.notes.dto;

import com.memora.memora_backend.notes.Note;
import com.memora.memora_backend.user.User;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponseDto toNoteResponseDto(Note note){
        NoteResponseDto noteResponseDto = new NoteResponseDto();
        noteResponseDto.setId(note.getId());
        noteResponseDto.setTitle(note.getTitle());
        noteResponseDto.setContent(note.getContent());
        return noteResponseDto;
    }

    public Note toNote(NoteRequestDto noteRequestDto){
        Note note = new Note();
        note.setTitle(noteRequestDto.getTitle());
        note.setContent(noteRequestDto.getContent());
        note.setUser(new User(noteRequestDto.getUser().getId()));
        return note;
    }
}
