package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.cursor.CursorPage;
import com.memora.memora_backend.cursor.CursorUtil;
import com.memora.memora_backend.multimedia.dto.MultimediaMapper;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.storage.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Save a multimedia and its thumbnail to the database and to the storage service
     * @param multimediaRequestDto the multimedia request dto
     * @param files the files to save
     * @return the saved multimedia response dto list
     */
    @Override
    public List<MultimediaResponseDto> save(MultimediaRequestDto multimediaRequestDto, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("At least one file is required");
        }

        List<Multimedia> savedEntities = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                var savedData = multimediaRepository.save(
                        multimediaMapper.toMultimedia(multimediaRequestDto, file)
                );

                storageService.uploadFile(file, savedData.getObjectKey());

                byte[] thumbnailBytes = createThumbnail(file);
                storageService.uploadFile(thumbnailBytes, savedData.getThumbnailObjectKey());

                savedEntities.add(savedData);
            }

            return savedEntities.stream()
                    .map(multimediaMapper::toMultimediaResponseDto)
                    .toList();

        } catch (Exception e) {
            for (Multimedia savedData : savedEntities) {
                try {
                    multimediaRepository.delete(savedData);
                } catch (Exception ignored) {
                }

                try {
                    storageService.deleteFile(savedData.getObjectKey());
                } catch (Exception ignored) {
                }

                try {
                    storageService.deleteFile(savedData.getThumbnailObjectKey());
                } catch (Exception ignored) {
                }
            }

            throw new RuntimeException("Error uploading images", e);
        }
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

    @Override
    public MultimediaResponseDto update(Long id,MultipartFile file) {
        //TODO Update needs to be refactored to use the same logic as the create method
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }

        storageService.deleteFile(multimedia.getObjectKey());
        var savedData = multimediaRepository.save(multimedia);
        return multimediaMapper.toMultimediaResponseDto(savedData);
    }

    /**
     * Find all multimedia entities with pagination
     * @param cursor the cursor to start from
     * @param limit the number of entities to return
     * @return the cursor page of multimedia entities
     */
    @Override
    public CursorPage<MultimediaResponseDto> findAll(String cursor, int limit) {

        var pageable = PageRequest.of(0, limit + 1);

        List<Multimedia> results;

        // If no cursor is provided, return entities paginated
        if (cursor == null) {
            results = multimediaRepository.findAllByOrderByUploadDateAscIdAsc(pageable);
        } else {
            // Decode the cursor and find the next page of entities
            var decoded = CursorUtil.decode(cursor);
            results = multimediaRepository.findNextPage(
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

    /**
     * Create a thumbnail for a given file
     * @param file the file to create a thumbnail for
     * @return the thumbnail as a byte array
     * @throws IOException if there is an error creating the thumbnail
     */
    private byte[] createThumbnail(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("File content type is missing");
        }

        // Process the image file
        if (contentType.startsWith("image/")) {
            return multimediaProcessingService.createImageThumbnail(file);
        }

        // Process the video file
        if (contentType.startsWith("video/")) {
            return multimediaProcessingService.createVideoThumbnail(file);
        }

        throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

}

