package com.memora.memora_backend.note.dto;

import com.memora.memora_backend.note.Note;
import com.memora.memora_backend.user.User;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class NoteMapper {

    /**
     * Maps a Note to a NoteResponseDto object.
     * @param note the note to be mapped
     * @return noteResponseDto object
     */
    public NoteResponseDto toNoteResponseDto(Note note){
        return NoteResponseDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(Date.from(note.getCreatedAt()))
                .updatedAt(Date.from(note.getUpdatedAt()))
                .build();
    }

    /**
     * Maps a NoteRequestDto object to a Note object
     * @param noteRequestDto the NoteRequestDto object to be mapped
     * @param user the user who created the note
     * @return the Note object
     */
    public Note toNote(NoteRequestDto noteRequestDto, User user){
        return Note.builder()
                .title(noteRequestDto.getTitle())
                .content(noteRequestDto.getContent())
                .user(user)
                .build();
    }
}
