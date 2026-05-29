package com.memora.memora_backend.note;

import com.memora.memora_backend.note.dto.NoteMapper;
import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;
import com.memora.memora_backend.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    private final UserRepository userRepository;

    private final NoteMapper noteMapper;

    @Override
    public NoteResponseDto save(NoteRequestDto noteRequestDto) {
        var user = userRepository.findById(noteRequestDto.getUser().getId()).orElse(null);

        if(user == null){
            throw new RuntimeException("User not found");
        }

        Note note = noteMapper.toNote(noteRequestDto,user);
        return noteMapper.toNoteResponseDto(noteRepository.save(note));
    }

    @Override
    public NoteResponseDto findById(Long id) {
        var note = noteRepository.findById(id).orElse(null);
        if(note == null){
            throw new RuntimeException("Note not found");
        }
        return noteMapper.toNoteResponseDto(note);
    }

    @Override
    public void delete(Long id) {
        noteRepository.deleteById(id);
    }

    @Override
    public void deleteAll(List<Long> ids) {
        //To be implemented
    }

    @Override
    public List<NoteResponseDto> findAll(Long userId) {
        List<Note> noteList = noteRepository.findAllByUserId(userId);
        return noteList.stream().map(noteMapper::toNoteResponseDto).toList();
    }

    @Override
    public NoteResponseDto update(NoteRequestDto noteRequestDto) {
        //To be implemented
        return null;
    }
}
