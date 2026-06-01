package com.memora.memora_backend.note;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(NoteController.class)
@ActiveProfiles("dev")
public class NoteControllerTest {
}
