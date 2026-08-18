package com.memora.memora_backend.note;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.cursor.CursorUtil;
import com.memora.memora_backend.note.dto.NoteMapper;
import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;
import com.memora.memora_backend.user.User;
import com.memora.memora_backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User mockUser;
    private Note mockNote;
    private NoteRequestDto requestDto;
    private NoteResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("user@example.com").build();

        mockNote = Note.builder()
                .id(10L)
                .title("Sample Title")
                .content("Sample Content")
                .updatedAt(Instant.now())
                .user(mockUser)
                .build();

        requestDto = new NoteRequestDto();
        requestDto.setUserId(1L);

        responseDto = NoteResponseDto.builder().id(10L).build();
    }

    // --- save() Tests ---

    @Test
    @DisplayName("save() - Should successfully save note and return response DTO")
    void testSave_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(noteMapper.toNote(requestDto, mockUser)).thenReturn(mockNote);
        when(noteRepository.save(mockNote)).thenReturn(mockNote);
        when(noteMapper.toNoteResponseDto(mockNote)).thenReturn(responseDto);

        NoteResponseDto result = noteService.save(requestDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());

        verify(userRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).save(mockNote);
    }

    @Test
    @DisplayName("save() - Should throw EntityNotFoundException when user does not exist")
    void testSave_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> noteService.save(requestDto)
        );

        assertTrue(exception.getMessage().contains("User not found with ID: 1"));
        verifyNoInteractions(noteRepository);
    }

    // --- findById() Tests ---

    @Test
    @DisplayName("findById() - Should return note response DTO when ID exists")
    void testFindById_Success() {
        when(noteRepository.findById(10L)).thenReturn(Optional.of(mockNote));
        when(noteMapper.toNoteResponseDto(mockNote)).thenReturn(responseDto);

        NoteResponseDto result = noteService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(noteRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("findById() - Should throw EntityNotFoundException when ID is not found")
    void testFindById_NotFound() {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> noteService.findById(99L)
        );

        assertTrue(exception.getMessage().contains("Note not found with ID: 99"));
        verify(noteRepository, times(1)).findById(99L);
    }

    // --- findAll() Tests ---

    @Test
    @DisplayName("findAll() - Should fetch first page without cursor")
    void testFindAll_FirstPage_NoCursor() {
        List<Note> queryResults = List.of(mockNote);

        when(noteRepository.findByUserIdOrderByUpdatedAtDescIdDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(queryResults);
        when(noteMapper.toNoteResponseDto(mockNote)).thenReturn(responseDto);

        try (MockedStatic<CursorUtil> cursorUtilMock = mockStatic(CursorUtil.class)) {
            cursorUtilMock.when(() -> CursorUtil.encode(any(Instant.class), any(Long.class)))
                    .thenReturn("encodedCursor");

            CursorPage<NoteResponseDto> result = noteService.findAll(1L, null, 10);

            assertNotNull(result);
            assertEquals(1, result.items().size());
            assertFalse(result.hasNext());
            assertEquals("encodedCursor", result.nextCursor());
        }

        verify(noteRepository, times(1)).findByUserIdOrderByUpdatedAtDescIdDesc(eq(1L), any(PageRequest.class));
    }

    @Test
    @DisplayName("findAll() - Should fetch next page with valid cursor and evaluate hasNext correctly")
    void testFindAll_WithCursor_AndHasNext() {
        Note item1 = Note.builder().id(10L).updatedAt(Instant.now()).build();
        Note item2 = Note.builder().id(11L).updatedAt(Instant.now()).build();
        List<Note> queryResults = List.of(item1, item2); // limit = 1, so 2 records mean hasNext = true

        try (MockedStatic<CursorUtil> cursorUtilMock = mockStatic(CursorUtil.class)) {
            cursorUtilMock.when(() -> CursorUtil.decode("validCursor"))
                    .thenReturn(Pair.of(Instant.now(), 100L));
            cursorUtilMock.when(() -> CursorUtil.encode(any(Instant.class), any(Long.class)))
                    .thenReturn("nextEncodedCursor");

            when(noteRepository.findNextPage(eq(1L), any(Instant.class), eq(100L), any(Pageable.class)))
                    .thenReturn(queryResults);
            when(noteMapper.toNoteResponseDto(item1)).thenReturn(responseDto);

            CursorPage<NoteResponseDto> result = noteService.findAll(1L, "validCursor", 1);

            assertNotNull(result);
            assertEquals(1, result.items().size());
            assertTrue(result.hasNext());
            assertEquals("nextEncodedCursor", result.nextCursor());
        }

        verify(noteRepository, times(1)).findNextPage(eq(1L), any(Instant.class), eq(100L), any(Pageable.class));
    }

    // --- delete() & deleteAll() Tests ---

    @Test
    @DisplayName("delete() - Should delete note when it exists")
    void testDelete_Exists() {
        when(noteRepository.existsById(10L)).thenReturn(true);
        doNothing().when(noteRepository).deleteById(10L);

        noteService.delete(10L);

        verify(noteRepository, times(1)).existsById(10L);
        verify(noteRepository, times(1)).deleteById(10L);
    }

    @Test
    @DisplayName("delete() - Should do nothing when note does not exist")
    void testDelete_DoesNotExist() {
        when(noteRepository.existsById(99L)).thenReturn(false);

        noteService.delete(99L);

        verify(noteRepository, times(1)).existsById(99L);
        verify(noteRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteAll() - Should delete all notes matching given IDs")
    void testDeleteAll() {
        List<Long> ids = List.of(10L, 11L);
        doNothing().when(noteRepository).deleteAllById(ids);

        noteService.deleteAll(ids);

        verify(noteRepository, times(1)).deleteAllById(ids);
    }

    // --- update() Test ---

    @Test
    @DisplayName("update() - Should throw UnsupportedOperationException")
    void testUpdate_ThrowsUnsupportedOperationException() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> noteService.update(requestDto)
        );
    }
}