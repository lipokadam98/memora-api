package com.memora.memora_backend.note;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.cursor.CursorUtil;
import com.memora.memora_backend.note.dto.NoteMapper;
import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;
import com.memora.memora_backend.user.User;
import com.memora.memora_backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
     * Retrieves a list of notes associated with a user. Supports pagination with a cursor.
     * @param userId the ID of the user to retrieve notes for
     * @param cursor the cursor string to use for pagination
     * @param limit the maximum number of notes to return per page
     * @return a CursorPage containing the list of notes and pagination information
     */
    @Override
    public CursorPage<NoteResponseDto> findAll(Long userId, String cursor, int limit) {
        var pageable = PageRequest.of(0, limit + 1);
        List<Note> results;

        if (cursor == null) {
            results = noteRepository.findByUserIdOrderByUpdatedAtDescIdDesc(userId, pageable);
        } else {
            var decoded = CursorUtil.decode(cursor);
            results = noteRepository.findNextPage(
                    userId,
                    decoded.getLeft(),
                    decoded.getRight(),
                    pageable
            );
        }

        boolean hasNext = results.size() > limit;
        if (hasNext) {
            results = results.subList(0, limit);
        }

        var noteResponseDtoList = results.stream()
                .map(noteMapper::toNoteResponseDto)
                .toList();

        String nextCursor = null;
        if (!results.isEmpty()) {
            var last = results.getLast();
            nextCursor = CursorUtil.encode(last.getUpdatedAt(), last.getId());
        }

        return new CursorPage<>(noteResponseDtoList, nextCursor, hasNext);
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