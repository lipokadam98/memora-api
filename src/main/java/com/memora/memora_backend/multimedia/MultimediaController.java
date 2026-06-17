package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.multimedia.dto.ThumbnailCreationRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/multimedia", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class MultimediaController {

    private final MultimediaService multimediaService;

    @Operation(
            summary = "Get all multimedia files paginated",
            description = "Fetches a page of user multimedia metadata records using an ascending chronological cursor-based pagination strategy."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated metadata records")
    })
    @GetMapping
    public CursorPage<MultimediaResponseDto> getAll(
            @Parameter(description = "ID of the user to filter files for", required = true) @RequestParam Long userId,
            @Parameter(description = "Base64 encoded cursor opaque string tracking page boundaries") @RequestParam(required = false) String cursor,
            @Parameter(description = "Maximum size of records to yield per page invocation") @RequestParam(defaultValue = "25") int limit
    ) {
        return multimediaService.findAll(userId, cursor, limit);
    }

    @Operation(summary = "Get a multimedia file by ID", description = "Retrieves metadata information for a single file record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found record"),
            @ApiResponse(responseCode = "404", description = "Multimedia record not found with given identifier")
    })
    @GetMapping("/{id}")
    public MultimediaResponseDto getById(
            @Parameter(description = "Primary key ID of the record", required = true) @PathVariable Long id
    ) {
        return multimediaService.findById(id);
    }

    @Operation(
            summary = "Batch record metadata",
            description = "Saves a list of multimedia information mappings to the relational database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Metadata batch stored successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload constraints or empty array passed")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<MultimediaResponseDto> create(@RequestBody List<MultimediaRequestDto> multimediaRequestDtoList) {
        return multimediaService.save(multimediaRequestDtoList);
    }

    @Operation(
            summary = "Batch generate thumbnails",
            description = "Processes an incoming pipeline batch to generate visual thumbnails asynchronously or synchronously for valid target files.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processing complete, yields successfully mutated targets")
    })
    @PostMapping("/create-thumbnails")
    public List<MultimediaResponseDto> createThumbnails(@RequestBody List<ThumbnailCreationRequestDto> thumbnailCreationRequestDtoList) {
        return multimediaService.createThumbnails(thumbnailCreationRequestDtoList);
    }

    @Operation(
            summary = "Delete an asset by ID",
            description = "Removes the tracking record from DB alongside destroying its associated object binaries inside active cloud storage buckets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Asset lifecycle ended cleanly; no representation context remains"),
            @ApiResponse(responseCode = "404", description = "Resource targets unknown")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Asset ID target", required = true) @PathVariable Long id) {
        multimediaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Batch delete assets",
            description = "Executes multi-record destruction sequences across both database states and object storage services.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Batch destruction processing finished cleanly")
    })
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids) {
        multimediaService.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }
}