package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.cursor.CursorUtil;
import com.memora.memora_backend.multimedia.dto.*;
import com.memora.memora_backend.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MultimediaServiceImpl implements MultimediaService{

    private final MultimediaRepository multimediaRepository;

    private final StorageService storageService;

    private final MultimediaMapper multimediaMapper;

    private final MultimediaProcessingService multimediaProcessingService;



    public MultimediaServiceImpl(MultimediaRepository multimediaRepository,
                                 StorageService storageService,
                                 MultimediaMapper multimediaMapper,
                                 MultimediaProcessingService multimediaProcessingService) {
        this.multimediaRepository = multimediaRepository;
        this.storageService = storageService;
        this.multimediaMapper = multimediaMapper;
        this.multimediaProcessingService = multimediaProcessingService;
    }

    @Transactional
    @Override
    public List<MultimediaResponseDto> save(List<MultimediaRequestDto> multimediaRequestDtoList) {
        if (multimediaRequestDtoList == null || multimediaRequestDtoList.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required");
        }

        // 1. Map DTOs to Entities
        List<Multimedia> entities = multimediaRequestDtoList.stream()
                .map(multimediaMapper::toMultimediaFromDto)
                .toList();

        // 2. Save all entities
        // Spring Data JPA saves these in the same transaction
        List<Multimedia> savedEntities = multimediaRepository.saveAll(entities);

        // 3. Return results
        return savedEntities.stream()
                .map(multimediaMapper::toMultimediaResponseDtoWithSignedUrl)
                .toList();
    }

    /**
     * Find a multimedia by id
     * @param id the id of the multimedia
     * @return the multimedia response dto
     */
    @Override
    public MultimediaResponseDto findById(Long id) {
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }
        return multimediaMapper.toMultimediaResponseDto(multimedia);
    }

    /**
     * Delete a multimedia and its thumbnail from the database and from the storage service
     * @param id the id of the multimedia to delete
     */
    @Override
    public void delete(Long id) {
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }
        storageService.deleteFile(multimedia.getObjectKey());
        storageService.deleteFile(multimedia.getThumbnailObjectKey());
        multimediaRepository.deleteById(id);
    }

    /**
     * Find all multimedia entities with pagination
     * @param cursor the cursor to start from
     * @param limit the number of entities to return
     * @return the cursor page of multimedia entities
     */
    @Override
    public CursorPage<MultimediaResponseDto> findAll(Long userId, String cursor, int limit) {

        var pageable = PageRequest.of(0, limit + 1);

        List<Multimedia> results;

        // If no cursor is provided, return entities paginated
        if (cursor == null) {
            results = multimediaRepository.findByUserIdOrderByUploadDateAscIdAsc(userId,pageable);
        } else {
            // Decode the cursor and find the next page of entities
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

        // Convert the entities to DTOs
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
     * Download the thumbnail of a multimedia
     * @param id the id of the multimedia
     * @return the thumbnail as a resource
     */
    @Override
    public Resource downloadThumbnail(Long id) {
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }
        return new InputStreamResource(storageService.downloadFile(multimedia.getThumbnailObjectKey()));
    }

    /**
     * Download the content of a multimedia
     * @param objectKey the object key of the multimedia
     * @return the multimedia content as a resource
     */
    @Override
    public Resource downloadContent(String objectKey) {
        return new InputStreamResource(storageService.downloadFile(objectKey));
    }

    @Override
    public List<MultimediaResponseDto> createThumbnails(List<ThumbnailCreationRequestDto> dtos) {
        // 1. Filter and get IDs upfront
        List<Long> ids = dtos.stream()
                .filter(dto -> dto.getStatus() != UploadStatus.FAILED)
                .map(ThumbnailCreationRequestDto::getId)
                .toList();

        // 2. Fetch all required entities in ONE query
        List<Multimedia> multimediaList = multimediaRepository.findAllById(ids);
        List<Multimedia> successfullyProcessed = new ArrayList<>();

        // 3. Process each item (with error isolation)
        for (Multimedia multimedia : multimediaList) {
            try {
                var objectKey = multimedia.getObjectKey();

                try (InputStream inputStream = storageService.downloadFile(objectKey)) {
                    var thumbnailByteArray = createThumbnailByteArray(multimedia.getContentType(), inputStream);
                    storageService.uploadFile(thumbnailByteArray, multimedia.getThumbnailObjectKey());
                    successfullyProcessed.add(multimedia);
                }
            } catch (Exception e) {
                // Log the error but continue processing the rest of the list
                log.error("Failed to create thumbnail for ID: {}", multimedia.getId(), e);
            }
        }

        return successfullyProcessed.stream()
                .map(multimediaMapper::toMultimediaResponseDto)
                .toList();
    }

    @Transactional
    @Override
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 1. Fetch the actual entities to get their storage keys
        List<Multimedia> multimediaList = multimediaRepository.findAllById(ids);

        if (multimediaList.isEmpty()) {
            return;
        }

        // 2. Remove files from storage
        for (Multimedia multimedia : multimediaList) {
            try {
                // Delete main content
                if (multimedia.getObjectKey() != null) {
                    storageService.deleteFile(multimedia.getObjectKey());
                }
                // Delete thumbnail
                if (multimedia.getThumbnailObjectKey() != null) {
                    storageService.deleteFile(multimedia.getThumbnailObjectKey());
                }
            } catch (Exception e) {
                // We log the error but continue to allow the DB deletion to proceed.
                // Depending on your requirements, you might want to rethrow to trigger a rollback.
                log.error("Failed to delete storage files for multimedia ID: {}", multimedia.getId(), e);
            }
        }

        // 3. Remove from database
        multimediaRepository.deleteAll(multimediaList);
    }


    private byte[] createThumbnailByteArray(String contentType, InputStream inputStream) throws IOException {
        if (contentType == null) {
            throw new IllegalArgumentException("File content type is missing");
        }

        if (contentType.startsWith("image/")) {
            return multimediaProcessingService.createImageThumbnail(inputStream);
        }

        if (contentType.startsWith("video/")) {
            return multimediaProcessingService.createVideoThumbnailFromStream(inputStream);
        }

        throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

}

