package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.auth.jwt.JwtService;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void testCreateAuthenticated() throws Exception {
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
}
