package com.memora.memora_backend.note;

import com.memora.memora_backend.note.dto.NoteMapper;
import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;
import com.memora.memora_backend.user.UserRepository;
import com.memora.memora_backend.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;

    /**
     * Persists a new note associated with a validated user.
     * * @throws EntityNotFoundException if the specified user does not exist.
     */
    @Override
    @Transactional
    public NoteResponseDto save(NoteRequestDto noteRequestDto) {
        Long userId = noteRequestDto.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Note note = noteMapper.toNote(noteRequestDto, user);
        Note savedNote = noteRepository.save(note);

        return noteMapper.toNoteResponseDto(savedNote);
    }

    /**
     * Retrieves a single note.
     * * @throws EntityNotFoundException if no note matches the provided ID.
     */
    @Override
    public NoteResponseDto findById(Long id) {
        return noteRepository.findById(id)
                .map(noteMapper::toNoteResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with ID: " + id));
    }

    /**
     * Retrieves all notes owned by a specific user.
     * Returns an empty list if the user has no notes.
     */
    @Override
    public List<NoteResponseDto> findAll(Long userId) {
        return noteRepository.findAllByUserId(userId)
                .stream()
                .map(noteMapper::toNoteResponseDto)
                .toList();
    }

    /**
     * Deletes a note by its ID.
     * Safe to call even if the ID does not exist (idempotent operation).
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // Using existsById prevents an unnecessary entity load before deletion
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {
        noteRepository.deleteAllById(ids);
    }

    @Override
    @Transactional
    public NoteResponseDto update(NoteRequestDto noteRequestDto) {
        // TODO: Implement update logic (e.g., fetch existing, map changes, save)
        throw new UnsupportedOperationException("Update operation not implemented yet.");
    }
}