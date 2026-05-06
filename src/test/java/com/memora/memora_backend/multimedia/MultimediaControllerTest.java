package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("POST /api/multimedia/ - Should fail with status code 403")
    void testCreateWithoutAuthentication() throws Exception {
        List<MultimediaRequestDto> requestList = List.of(new MultimediaRequestDto());
        List<MultimediaResponseDto> responseList = List.of(new MultimediaResponseDto());

        when(multimediaService.save(anyList())).thenReturn(responseList);

        mockMvc.perform(post("/api/multimedia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestList)))
                        .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/multimedia/ - Should create list of multimedia items")
    void testCreate() throws Exception {
        List<MultimediaRequestDto> requestList = List.of(new MultimediaRequestDto());
        List<MultimediaResponseDto> responseList = List.of(new MultimediaResponseDto());

        when(multimediaService.save(anyList())).thenReturn(responseList);

        mockMvc.perform(post("/api/multimedia")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestList)))
                        .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("GET /api/multimedia/{id} - Should return a single multimedia object")
    void testGetById() throws Exception {
        MultimediaResponseDto responseDto = new MultimediaResponseDto();
        responseDto.setId(1L);

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
}
