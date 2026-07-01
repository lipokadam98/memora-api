package com.memora.memora_backend.note;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.note.dto.NoteRequestDto;
import com.memora.memora_backend.note.dto.NoteResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @Operation(
            summary = "Get all notes paginated",
            description = "Fetches a page of user note records using an ascending chronological cursor-based pagination strategy."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated metadata records")
    })
    @GetMapping
    public CursorPage<NoteResponseDto> getAll(
            @Parameter(description = "ID of the user to filter notes for", required = true) @RequestParam Long userId,
            @Parameter(description = "Base64 encoded cursor opaque string tracking page boundaries") @RequestParam(required = false) String cursor,
            @Parameter(description = "Maximum size of records to yield per page invocation") @RequestParam(defaultValue = "25") int limit
    ) {
        return noteService.findAll(userId, cursor, limit);
    }

    @Operation(
            summary = "Get a note by ID",
            description = "Retrieves a single note's details using its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the note"),
            @ApiResponse(responseCode = "404", description = "Note not found with the given ID")
    })
    @GetMapping("/{id}")
    public NoteResponseDto getById(
            @Parameter(description = "Unique ID of the note", required = true)
            @PathVariable Long id
    ) {
        return noteService.findById(id);
    }

    @Operation(
            summary = "Create a new note",
            description = "Creates a new note record based on the provided request payload."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Note successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload supplied")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponseDto create(@RequestBody NoteRequestDto noteRequestDto) {
        return noteService.save(noteRequestDto);
    }

    @Operation(
            summary = "Delete a note by ID",
            description = "Permanently deletes a note record from the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Note successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Note not found with the given ID")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Unique ID of the note to be deleted", required = true)
            @PathVariable Long id
    ) {
        noteService.delete(id);
    }
}