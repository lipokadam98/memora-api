package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MultimediaServiceImpl implements MultimediaService{

    @Value("${cloud.bucketName}")
    private String bucketName;

    @Value("${base-url}")
    private String baseUrl;

    private final MultimediaRepository multimediaRepository;

    private final StorageService storageService;

    public MultimediaServiceImpl(MultimediaRepository multimediaRepository,
                                 StorageService storageService) {
        this.multimediaRepository = multimediaRepository;
        this.storageService = storageService;
    }

    @Override
    public Multimedia save(MultimediaRequestDto multimediaRequestDto, MultipartFile file) {
        String objectKey = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Multimedia multimedia = new Multimedia();
        multimedia.setBucketName(bucketName);
        multimedia.setSize(file.getSize());
        multimedia.setContentType(file.getContentType());
        multimedia.setOriginalFileName(file.getOriginalFilename());
        multimedia.setUser(multimediaRequestDto.getUser());
        multimedia.setObjectKey(objectKey);

        // TODO error handling for file upload
        var savedData = multimediaRepository.save(multimedia);

        try {
            this.storageService.uploadFile(file,objectKey);
        } catch(Exception e){
            multimediaRepository.delete(savedData);
            throw new RuntimeException("Error uploading file");
        }

        return savedData;
    }

    @Override
    public Multimedia findById(Long id) {
        return multimediaRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        multimediaRepository.deleteById(id);
    }

    @Override
    public Multimedia update(Multimedia multimedia) {
        return multimediaRepository.save(multimedia);
    }

    @Override
    public List<MultimediaResponseDto> findAll() {

        var multimediaList = multimediaRepository.findAll();
        var multimediaResponseDtoList = new ArrayList<MultimediaResponseDto>();

        for(Multimedia multimedia : multimediaList){
            MultimediaResponseDto multimediaResponseDto = new MultimediaResponseDto();
            multimediaResponseDto.setId(multimedia.getId());
            multimediaResponseDtoList.add(multimediaResponseDto);
            multimediaResponseDto.setImageUrl(baseUrl+"/multimedia/" + multimedia.getId() + "/thumbnail");
        }

        return multimediaResponseDtoList;
    }

    @Override
    public Resource getThumbnail(Long id) {
        Multimedia multimedia = findById(id);
        return new InputStreamResource(storageService.downloadFile(multimedia.getObjectKey()));
    }

}

