package com.memora.memora_backend.multimedia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MultimediaController.class)
public class MultimediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MultimediaService multimediaService;
}
