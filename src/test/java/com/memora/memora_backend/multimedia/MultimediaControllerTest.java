package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.multimedia.dto.ThumbnailCreationRequestDto;
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MultimediaController.class)
@ActiveProfiles("dev")
public class MultimediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MultimediaService multimediaService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    private final MultimediaResponseDto responseDto = MultimediaResponseDto.builder().id(1L).build();

    @Test
    @WithMockUser
    @DisplayName("GET /api/multimedia - Should return paginated multimedia")
    void testGetAll() throws Exception {
        List<MultimediaResponseDto> multimediaResponseDtoList = List.of(responseDto, responseDto);
        CursorPage<MultimediaResponseDto> response = new CursorPage<>(multimediaResponseDtoList, "cursor", true);
        when(multimediaService.findAll(1L,"cursor",2)).thenReturn(response);

        mockMvc.perform(get("/api/multimedia")
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
    @DisplayName("GET /api/multimedia/{id} - Should return a single multimedia object")
    void testGetById() throws Exception {
        when(multimediaService.findById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/multimedia/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/multimedia/{id} - Should throw an error for not found multimedia")
    void testGetById_NotFound() throws Exception {
        when(multimediaService.findById(99L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/multimedia/99"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /api/multimedia/ - Should fail with status code 403")
    void testCreate_WithoutAuthentication() throws Exception {
        List<MultimediaRequestDto> requestList = List.of(new MultimediaRequestDto());
        List<MultimediaResponseDto> responseList = List.of(responseDto);

        when(multimediaService.save(anyList())).thenReturn(responseList);

        mockMvc.perform(post("/api/multimedia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestList)))
                        .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/multimedia/ - Should create list of multimedia items")
    void testCreate() throws Exception {
        List<MultimediaRequestDto> requestList = List.of(new MultimediaRequestDto());
        List<MultimediaResponseDto> responseList = List.of(responseDto);

        when(multimediaService.save(anyList())).thenReturn(responseList);

        mockMvc.perform(post("/api/multimedia")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestList)))
                        .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/multimedia/create-thumbnails - Should create list of thumbnails")
    void testCreateThumbnails() throws Exception {
        List<ThumbnailCreationRequestDto> requestList = List.of(new ThumbnailCreationRequestDto());
        List<MultimediaResponseDto> responseList = List.of(responseDto);

        when(multimediaService.createThumbnails(requestList)).thenReturn(responseList);

        mockMvc.perform(post("/api/multimedia")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestList)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/multimedia/{id} - Should delete a simple multimedia item")
    void testSingleMultimediaDelete() throws Exception {
        Long targetId = 1L;

        doNothing().when(multimediaService).delete(targetId);

        mockMvc.perform(delete("/api/multimedia/{id}", targetId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(multimediaService, times(1)).delete(targetId);
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/multimedia/batch - Should delete a list of multimedia items")
    void testListOfMultimediaDelete() throws Exception {
        List<Long> targetIds = List.of(1L, 2L);

        doNothing().when(multimediaService).deleteAll(targetIds);

        mockMvc.perform(delete("/api/multimedia/batch")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(targetIds))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(multimediaService, times(1)).deleteAll(targetIds);
    }


}
