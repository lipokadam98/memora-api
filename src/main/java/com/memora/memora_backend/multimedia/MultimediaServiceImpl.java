package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.cursor.CursorUtil;
import com.memora.memora_backend.multimedia.dto.*;
import com.memora.memora_backend.storage.StorageService;
import com.memora.memora_backend.user.UserRepository;
import com.memora.memora_backend.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class MultimediaServiceImpl implements MultimediaService {

    private final MultimediaRepository multimediaRepository;
    private final StorageService storageService;
    private final MultimediaMapper multimediaMapper;
    private final MultimediaProcessingService multimediaProcessingService;
    private final UserRepository userRepository;

    /**
     * Maps and persists a collective sequence of new multimedia metadata links.
     * @throws IllegalArgumentException if payload sequence is null or evaluates empty.
     * @throws EntityNotFoundException if structural User identity context does not map to database profiles.
     */
    @Transactional
    @Override
    public List<MultimediaResponseDto> save(List<MultimediaRequestDto> multimediaRequestDtoList) {
        if (multimediaRequestDtoList == null || multimediaRequestDtoList.isEmpty()) {
            throw new IllegalArgumentException("At least one file mapping definition is required");
        }

        // Defensive boundary check isolating User identity assignment context safely
        Long userId = multimediaRequestDtoList.getFirst().getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User registration state not resolved for ID: " + userId));

        List<Multimedia> entities = multimediaRequestDtoList.stream()
                .map(request -> multimediaMapper.toMultimediaFromDto(request, user))
                .toList();

        List<Multimedia> savedEntities = multimediaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(multimediaMapper::toMultimediaResponseDtoWithSignedUrl)
                .toList();
    }

    /**
     * Looks up an individual multimedia registry model wrapper.
     * @throws EntityNotFoundException when reference profile mappings fail to match.
     */
    @Override
    public MultimediaResponseDto findById(Long id) {
        return multimediaRepository.findById(id)
                .map(multimediaMapper::toMultimediaResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Multimedia file trace not found for ID: " + id));
    }

    /**
     * Destroys both cloud store blobs (main target + corresponding thumbnail) before purging DB metadata rows.
     * @throws EntityNotFoundException when targeted resource ID traces do not resolve.
     */
    @Transactional
    @Override
    public void delete(Long id) {
        Multimedia multimedia = multimediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Multimedia file trace not found for ID: " + id));

        // Delete artifacts out of object store systems cleanly before handling DB purges
        storageService.deleteFile(multimedia.getObjectKey());
        storageService.deleteFile(multimedia.getThumbnailObjectKey());

        multimediaRepository.delete(multimedia);
    }

    /**
     * Drives lookups using key-ordered forward scrolling windows to guard query performance at scale.
     */
    @Override
    public CursorPage<MultimediaResponseDto> findAll(Long userId, String cursor, int limit) {
        // Fetch limit + 1 records to evaluate prospective continuation pages smoothly without counts
        var pageable = PageRequest.of(0, limit + 1);
        List<Multimedia> results;

        if (cursor == null) {
            results = multimediaRepository.findByUserIdOrderByUploadDateAscIdAsc(userId, pageable);
        } else {
            var decoded = CursorUtil.decode(cursor);
            results = multimediaRepository.findNextPage(
                    userId,
                    decoded.getLeft(),
                    decoded.getRight(),
                    pageable
            );
        }

        boolean hasNext = results.size() > limit;
        if (hasNext) {
            results = results.subList(0, limit);
        }

        var multimediaResponseDtoList = results.stream()
                .map(multimediaMapper::toMultimediaResponseDto)
                .toList();

        String nextCursor = null;
        if (!results.isEmpty()) {
            var last = results.getLast();
            nextCursor = CursorUtil.encode(last.getUploadDate(), last.getId());
        }

        return new CursorPage<>(multimediaResponseDtoList, nextCursor, hasNext);
    }

    /**
     * Generates cloud storage thumbnails processing streams with isolated error loops.
     */
    @Transactional
    @Override
    public List<MultimediaResponseDto> createThumbnails(List<ThumbnailCreationRequestDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        List<Long> ids = dtos.stream()
                .filter(dto -> dto.getStatus() != UploadStatus.FAILED)
                .map(ThumbnailCreationRequestDto::getId)
                .toList();

        List<Multimedia> multimediaList = multimediaRepository.findAllById(ids);
        List<Multimedia> successfullyProcessed = new ArrayList<>();

        for (Multimedia multimedia : multimediaList) {
            try {
                String objectKey = multimedia.getObjectKey();

                try (InputStream inputStream = storageService.downloadFile(objectKey)) {
                    byte[] thumbnailByteArray = createThumbnailByteArray(multimedia.getContentType(), inputStream);
                    storageService.uploadFile(thumbnailByteArray, multimedia.getThumbnailObjectKey());
                    successfullyProcessed.add(multimedia);
                }
            } catch (Exception e) {
                // Isolated pipeline protection ensuring individual failures do not disrupt concurrent stream handling
                log.error("Failed to generate isolated asset thumbnail compilation variant for ID: {}", multimedia.getId(), e);
            }
        }

        return successfullyProcessed.stream()
                .map(multimediaMapper::toMultimediaResponseDto)
                .toList();
    }

    /**
     * Synchronizes cloud binary purges before execution arrays drop from table index schemas.
     */
    @Transactional
    @Override
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Multimedia> multimediaList = multimediaRepository.findAllById(ids);
        if (multimediaList.isEmpty()) {
            return;
        }

        for (Multimedia multimedia : multimediaList) {
            try {
                if (multimedia.getObjectKey() != null) {
                    storageService.deleteFile(multimedia.getObjectKey());
                }
                if (multimedia.getThumbnailObjectKey() != null) {
                    storageService.deleteFile(multimedia.getThumbnailObjectKey());
                }
            } catch (Exception e) {
                log.error("Stale tracking cleanup exception bypassed on blob path destruction sequence for Asset ID: {}", multimedia.getId(), e);
            }
        }

        multimediaRepository.deleteAllInBatch(multimediaList);
    }

    /**
     * Delegates specific graphic arrays or stream slice handling down to isolated processors.
     */
    private byte[] createThumbnailByteArray(String contentType, InputStream inputStream) throws IOException {
        if (contentType == null) {
            throw new IllegalArgumentException("File stream content type definition is missing");
        }

        if (contentType.startsWith("image/")) {
            return multimediaProcessingService.createImageThumbnail(inputStream);
        }

        if (contentType.startsWith("video/")) {
            return multimediaProcessingService.createVideoThumbnailFromStream(inputStream);
        }

        throw new IllegalArgumentException("Unsupported system ingestion mimetype layout variant: " + contentType);
    }
}