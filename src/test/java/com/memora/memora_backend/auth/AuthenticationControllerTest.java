package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.LoginResponse;
import com.memora.memora_backend.auth.dto.LoginUserDto;
import com.memora.memora_backend.auth.dto.RegisterUserDto;
import com.memora.memora_backend.auth.dto.UserDto;
import com.memora.memora_backend.auth.jwt.JwtService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@ActiveProfiles("dev")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    private final UserDto userDto = UserDto.builder().id(1L).email("test@example.com").build();
    private final LoginResponse loginResponse = LoginResponse.builder().token("mocked-jwt-token").build();

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/signup - Should register a new user successfully")
    void testRegister() throws Exception {
        RegisterUserDto registerUserDto = new RegisterUserDto();

        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - Should authenticate user and return token response")
    void testAuthenticate() throws Exception {
        LoginUserDto loginUserDto = new LoginUserDto();

        when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - Should handle invalid credentials failure")
    void testAuthenticate_Unauthorized() throws Exception {
        LoginUserDto loginUserDto = new LoginUserDto();

        when(authenticationService.authenticate(any(LoginUserDto.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }
}