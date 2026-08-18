package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.cursor.CursorUtil;
import com.memora.memora_backend.multimedia.dto.*;
import com.memora.memora_backend.storage.StorageService;
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
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MultimediaServiceImplTest {

    @Mock
    private MultimediaRepository multimediaRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private MultimediaMapper multimediaMapper;

    @Mock
    private MultimediaProcessingService multimediaProcessingService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MultimediaServiceImpl multimediaService;

    private User mockUser;
    private Multimedia mockMultimedia;
    private MultimediaRequestDto requestDto;
    private MultimediaResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("user@example.com").build();

        mockMultimedia = Multimedia.builder()
                .id(10L)
                .objectKey("files/image1.jpg")
                .thumbnailObjectKey("thumbnails/image1.jpg")
                .contentType("image/jpeg")
                .uploadDate(Instant.now())
                .user(mockUser)
                .build();

        requestDto = new MultimediaRequestDto();
        requestDto.setUserId(1L);

        responseDto = MultimediaResponseDto.builder().id(10L).build();
    }

    // --- save() Tests ---

    @Test
    @DisplayName("save() - Should successfully save multimedia list and return response DTOs with signed URLs")
    void testSave_Success() {
        List<MultimediaRequestDto> requestList = List.of(requestDto);
        List<Multimedia> entityList = List.of(mockMultimedia);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(multimediaMapper.toMultimediaFromDto(requestDto, mockUser)).thenReturn(mockMultimedia);
        when(multimediaRepository.saveAll(entityList)).thenReturn(entityList);
        when(multimediaMapper.toMultimediaResponseDtoWithSignedUrl(mockMultimedia)).thenReturn(responseDto);

        List<MultimediaResponseDto> result = multimediaService.save(requestList);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getId());

        verify(userRepository, times(1)).findById(1L);
        verify(multimediaRepository, times(1)).saveAll(entityList);
    }

    @Test
    @DisplayName("save() - Should throw IllegalArgumentException when request list is null or empty")
    void testSave_NullOrEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> multimediaService.save(null));
        assertThrows(IllegalArgumentException.class, () -> multimediaService.save(Collections.emptyList()));

        verifyNoInteractions(userRepository, multimediaRepository);
    }

    @Test
    @DisplayName("save() - Should throw EntityNotFoundException when user is not found")
    void testSave_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> multimediaService.save(List.of(requestDto)));

        verify(userRepository, times(1)).findById(1L);
        verifyNoInteractions(multimediaRepository);
    }

    // --- findById() Tests ---

    @Test
    @DisplayName("findById() - Should return multimedia response DTO when ID exists")
    void testFindById_Success() {
        when(multimediaRepository.findById(10L)).thenReturn(Optional.of(mockMultimedia));
        when(multimediaMapper.toMultimediaResponseDto(mockMultimedia)).thenReturn(responseDto);

        MultimediaResponseDto result = multimediaService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(multimediaRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("findById() - Should throw EntityNotFoundException when ID is not found")
    void testFindById_NotFound() {
        when(multimediaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> multimediaService.findById(99L));
        verify(multimediaRepository, times(1)).findById(99L);
    }

    // --- delete() Tests ---

    @Test
    @DisplayName("delete() - Should delete storage files and database entity")
    void testDelete_Success() {
        when(multimediaRepository.findById(10L)).thenReturn(Optional.of(mockMultimedia));
        doNothing().when(storageService).deleteFile("files/image1.jpg");
        doNothing().when(storageService).deleteFile("thumbnails/image1.jpg");
        doNothing().when(multimediaRepository).delete(mockMultimedia);

        multimediaService.delete(10L);

        verify(storageService, times(1)).deleteFile("files/image1.jpg");
        verify(storageService, times(1)).deleteFile("thumbnails/image1.jpg");
        verify(multimediaRepository, times(1)).delete(mockMultimedia);
    }

    @Test
    @DisplayName("delete() - Should throw EntityNotFoundException when entity to delete is missing")
    void testDelete_NotFound() {
        when(multimediaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> multimediaService.delete(99L));
        verifyNoInteractions(storageService);
    }

    // --- findAll() Tests ---

    @Test
    @DisplayName("findAll() - Should fetch first page without cursor")
    void testFindAll_FirstPage_NoCursor() {
        List<Multimedia> queryResults = List.of(mockMultimedia);

        when(multimediaRepository.findByUserIdOrderByUploadDateDescIdDesc(eq(1L), any(Pageable.class)))
                .thenReturn(queryResults);
        when(multimediaMapper.toMultimediaResponseDto(mockMultimedia)).thenReturn(responseDto);

        try (MockedStatic<CursorUtil> cursorUtilMock = mockStatic(CursorUtil.class)) {
            cursorUtilMock.when(() -> CursorUtil.encode(any(Instant.class), any(Long.class)))
                    .thenReturn("encodedCursor");

            CursorPage<MultimediaResponseDto> result = multimediaService.findAll(1L, null, 10);

            assertNotNull(result);
            assertEquals(1, result.items().size());
            assertFalse(result.hasNext());
            assertEquals("encodedCursor", result.nextCursor());
        }

        verify(multimediaRepository, times(1)).findByUserIdOrderByUploadDateDescIdDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("findAll() - Should fetch next page with valid cursor and compute hasNext correctly")
    void testFindAll_WithCursor_AndHasNext() {
        Multimedia item1 = Multimedia.builder().id(10L).uploadDate(Instant.now()).build();
        Multimedia item2 = Multimedia.builder().id(11L).uploadDate(Instant.now()).build();
        List<Multimedia> queryResults = List.of(item1, item2); // limit = 1, so 2 records mean hasNext = true

        try (MockedStatic<CursorUtil> cursorUtilMock = mockStatic(CursorUtil.class)) {
            cursorUtilMock.when(() -> CursorUtil.decode("validCursor"))
                    .thenReturn(Pair.of(Instant.now(), 100L));
            cursorUtilMock.when(() -> CursorUtil.encode(any(Instant.class), any(Long.class)))
                    .thenReturn("nextEncodedCursor");

            when(multimediaRepository.findNextPage(eq(1L), any(Instant.class), eq(100L), any(Pageable.class)))
                    .thenReturn(queryResults);
            when(multimediaMapper.toMultimediaResponseDto(item1)).thenReturn(responseDto);

            CursorPage<MultimediaResponseDto> result = multimediaService.findAll(1L, "validCursor", 1);

            assertNotNull(result);
            assertEquals(1, result.items().size());
            assertTrue(result.hasNext());
            assertEquals("nextEncodedCursor", result.nextCursor());
        }

        verify(multimediaRepository, times(1)).findNextPage(eq(1L), any(Instant.class), eq(100L), any(Pageable.class));
    }

    // --- createThumbnails() Tests ---

    @Test
    @DisplayName("createThumbnails() - Should process image thumbnail creation successfully")
    void testCreateThumbnails_Image_Success() throws Exception {
        ThumbnailCreationRequestDto dto = new ThumbnailCreationRequestDto();
        dto.setId(10L);
        dto.setStatus(UploadStatus.DONE);

        InputStream mockStream = new ByteArrayInputStream("image data".getBytes());
        byte[] mockThumbnailBytes = "thumbnail bytes".getBytes();

        when(multimediaRepository.findAllById(List.of(10L))).thenReturn(List.of(mockMultimedia));
        when(storageService.downloadFile("files/image1.jpg")).thenReturn(mockStream);
        when(multimediaProcessingService.createImageThumbnail(any(InputStream.class))).thenReturn(mockThumbnailBytes);
        doNothing().when(storageService).uploadFile(mockThumbnailBytes, "thumbnails/image1.jpg");
        when(multimediaMapper.toMultimediaResponseDto(mockMultimedia)).thenReturn(responseDto);

        List<MultimediaResponseDto> result = multimediaService.createThumbnails(List.of(dto));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(storageService, times(1)).uploadFile(mockThumbnailBytes, "thumbnails/image1.jpg");
    }

    @Test
    @DisplayName("createThumbnails() - Should process video thumbnail creation successfully")
    void testCreateThumbnails_Video_Success() throws Exception {
        Multimedia videoMultimedia = Multimedia.builder()
                .id(20L)
                .objectKey("files/video1.mp4")
                .thumbnailObjectKey("thumbnails/video1.jpg")
                .contentType("video/mp4")
                .build();

        ThumbnailCreationRequestDto dto = new ThumbnailCreationRequestDto();
        dto.setId(20L);
        dto.setStatus(UploadStatus.DONE);

        InputStream mockStream = new ByteArrayInputStream("video data".getBytes());
        byte[] mockThumbnailBytes = "thumbnail bytes".getBytes();

        when(multimediaRepository.findAllById(List.of(20L))).thenReturn(List.of(videoMultimedia));
        when(storageService.downloadFile("files/video1.mp4")).thenReturn(mockStream);
        when(multimediaProcessingService.createVideoThumbnailFromStream(any(InputStream.class))).thenReturn(mockThumbnailBytes);
        when(multimediaMapper.toMultimediaResponseDto(videoMultimedia)).thenReturn(responseDto);

        List<MultimediaResponseDto> result = multimediaService.createThumbnails(List.of(dto));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(multimediaProcessingService, times(1)).createVideoThumbnailFromStream(any(InputStream.class));
    }

    @Test
    @DisplayName("createThumbnails() - Should ignore items with FAILED upload status and empty list inputs")
    void testCreateThumbnails_FilterFailedAndEmptyInput() {
        ThumbnailCreationRequestDto failedDto = new ThumbnailCreationRequestDto();
        failedDto.setId(99L);
        failedDto.setStatus(UploadStatus.FAILED);

        when(multimediaRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<MultimediaResponseDto> result = multimediaService.createThumbnails(List.of(failedDto));

        assertTrue(result.isEmpty());
        assertTrue(multimediaService.createThumbnails(null).isEmpty());
        assertTrue(multimediaService.createThumbnails(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("createThumbnails() - Should isolate and skip failing items during thumbnail processing")
    void testCreateThumbnails_IsolatedFailure() {
        ThumbnailCreationRequestDto dto = new ThumbnailCreationRequestDto();
        dto.setId(10L);
        dto.setStatus(UploadStatus.DONE);

        when(multimediaRepository.findAllById(List.of(10L))).thenReturn(List.of(mockMultimedia));
        when(storageService.downloadFile(anyString())).thenThrow(new RuntimeException("Storage unavailable"));

        List<MultimediaResponseDto> result = multimediaService.createThumbnails(List.of(dto));

        assertTrue(result.isEmpty()); // Failed item skipped
        verify(storageService, times(1)).downloadFile("files/image1.jpg");
    }

    // --- deleteAll() Tests ---

    @Test
    @DisplayName("deleteAll() - Should delete storage files and batch delete database entities")
    void testDeleteAll_Success() {
        List<Long> ids = List.of(10L);
        List<Multimedia> list = List.of(mockMultimedia);

        when(multimediaRepository.findAllById(ids)).thenReturn(list);
        doNothing().when(storageService).deleteFile("files/image1.jpg");
        doNothing().when(storageService).deleteFile("thumbnails/image1.jpg");
        doNothing().when(multimediaRepository).deleteAllInBatch(list);

        multimediaService.deleteAll(ids);

        verify(storageService, times(1)).deleteFile("files/image1.jpg");
        verify(storageService, times(1)).deleteFile("thumbnails/image1.jpg");
        verify(multimediaRepository, times(1)).deleteAllInBatch(list);
    }

    @Test
    @DisplayName("deleteAll() - Should safely ignore null or empty ID lists")
    void testDeleteAll_NullOrEmptyInput() {
        multimediaService.deleteAll(null);
        multimediaService.deleteAll(Collections.emptyList());

        verifyNoInteractions(multimediaRepository, storageService);
    }

    @Test
    @DisplayName("deleteAll() - Should continue batch cleanup when storage deletion fails for an item")
    void testDeleteAll_StorageExceptionSwallowed() {
        List<Long> ids = List.of(10L);
        List<Multimedia> list = List.of(mockMultimedia);

        when(multimediaRepository.findAllById(ids)).thenReturn(list);
        doThrow(new RuntimeException("Cloud deletion error")).when(storageService).deleteFile(anyString());
        doNothing().when(multimediaRepository).deleteAllInBatch(list);

        assertDoesNotThrow(() -> multimediaService.deleteAll(ids));
        verify(multimediaRepository, times(1)).deleteAllInBatch(list);
    }
}