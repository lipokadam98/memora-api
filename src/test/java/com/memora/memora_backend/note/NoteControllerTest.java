package com.memora.memora_backend.note;

import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@ActiveProfiles("dev")
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    private final NoteResponseDto responseDto = NoteResponseDto.builder().id(1L).build();

    @Test
    @WithMockUser
    @DisplayName("GET /api/notes - Should return paginated notes")
    void testGetAll() throws Exception {
        List<NoteResponseDto> noteResponseDtoList = List.of(responseDto, responseDto);
        CursorPage<NoteResponseDto> response = new CursorPage<>(noteResponseDtoList, "cursor", true);
        when(noteService.findAll(1L, "cursor", 2)).thenReturn(response);

        mockMvc.perform(get("/api/notes")
                        .param("userId", "1")
                        .param("cursor", "cursor")
                        .param("limit", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/notes/{id} - Should return a single note object")
    void testGetById() throws Exception {
        when(noteService.findById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/notes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/notes/{id} - Should throw an error for not found note")
    void testGetById_NotFound() throws Exception {
        when(noteService.findById(99L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/notes/99"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /api/notes - Should fail with status code 403 when unauthenticated")
    void testCreate_WithoutAuthentication() throws Exception {
        NoteRequestDto requestDto = new NoteRequestDto();

        when(noteService.save(any(NoteRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/notes - Should create a note and return status 201")
    void testCreate() throws Exception {
        NoteRequestDto requestDto = new NoteRequestDto();

        when(noteService.save(any(NoteRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/notes/{id} - Should delete a note and return status 204")
    void testDelete() throws Exception {
        Long targetId = 1L;

        doNothing().when(noteService).delete(targetId);

        mockMvc.perform(delete("/api/notes/{id}", targetId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(noteService, times(1)).delete(targetId);
    }
}