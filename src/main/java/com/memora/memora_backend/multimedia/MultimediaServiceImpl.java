package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaMapper;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.storage.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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

    //TODO Add support multiple files
    @Override
    public MultimediaResponseDto save(MultimediaRequestDto multimediaRequestDto, MultipartFile file) {
        var savedData = multimediaRepository.save(
                multimediaMapper.toMultimedia(multimediaRequestDto, file)
        );

        try {
            storageService.uploadFile(file, savedData.getObjectKey());

            byte[] thumbnailBytes = createThumbnail(file);

            storageService.uploadFile(thumbnailBytes, savedData.getThumbnailObjectKey());

            return multimediaMapper.toMultimediaResponseDto(savedData);
        } catch (Exception e) {
            multimediaRepository.delete(savedData);
            storageService.deleteFile(savedData.getObjectKey());
            storageService.deleteFile(savedData.getThumbnailObjectKey());
            throw new RuntimeException("Error uploading image", e);
        }
    }

    @Override
    public MultimediaResponseDto findById(Long id) {
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }
        return multimediaMapper.toMultimediaResponseDto(multimedia);
    }

    @Override
    public void delete(Long id) {
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }
        multimediaRepository.deleteById(id);
        storageService.deleteFile(multimedia.getObjectKey());
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

    @Override
    public List<MultimediaResponseDto> findAll() {

        var multimediaList = multimediaRepository.findAll();
        var multimediaResponseDtoList = new ArrayList<MultimediaResponseDto>();

        for(Multimedia multimedia : multimediaList){
            var multimediaResponseDto = multimediaMapper.toMultimediaResponseDto(multimedia);
            multimediaResponseDtoList.add(multimediaResponseDto);
        }

        return multimediaResponseDtoList;
    }

    @Override
    public Resource downloadThumbnail(Long id) {
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }
        return new InputStreamResource(storageService.downloadFile(multimedia.getThumbnailObjectKey()));
    }

    @Override
    public Resource downloadContent(String objectKey) {
        return new InputStreamResource(storageService.downloadFile(objectKey));
    }

    private byte[] createThumbnail(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("File content type is missing");
        }

        if (contentType.startsWith("image/")) {
            return multimediaProcessingService.createImageThumbnail(file);
        }

        if (contentType.startsWith("video/")) {
            return multimediaProcessingService.createVideoThumbnail(file);
        }

        throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

}

