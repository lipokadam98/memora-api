package com.memora.memora_backend.notes.dto;

import com.memora.memora_backend.notes.Note;
import com.memora.memora_backend.user.User;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponseDto toNoteResponseDto(Note note){
        return NoteResponseDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .build();
    }

    public Note toNote(NoteRequestDto noteRequestDto,User user){
        return Note.builder()
                .title(noteRequestDto.getTitle())
                .content(noteRequestDto.getContent())
                .user(user)
                .build();
    }
}
