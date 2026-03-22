package com.memora.memora_backend.multimedia;

import com.memora.memora_backend.multimedia.dto.MultimediaMapper;
import com.memora.memora_backend.multimedia.dto.MultimediaRequestDto;
import com.memora.memora_backend.multimedia.dto.MultimediaResponseDto;
import com.memora.memora_backend.storage.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class MultimediaServiceImpl implements MultimediaService{

    private final MultimediaRepository multimediaRepository;

    private final StorageService storageService;

    private final MultimediaMapper multimediaMapper;

    public MultimediaServiceImpl(MultimediaRepository multimediaRepository,
                                 StorageService storageService,
                                 MultimediaMapper multimediaMapper) {
        this.multimediaRepository = multimediaRepository;
        this.storageService = storageService;
        this.multimediaMapper = multimediaMapper;
    }

    @Override
    public MultimediaResponseDto save(MultimediaRequestDto multimediaRequestDto, MultipartFile file) {
        var savedData = multimediaRepository.save(multimediaMapper.toMultimedia(multimediaRequestDto,file));
        var multimediaResponseDto = multimediaMapper.toMultimediaResponseDto(savedData);
        try {
            this.storageService.uploadFile(file,savedData.getObjectKey());
        } catch(Exception e){
            multimediaRepository.delete(savedData);
            throw new RuntimeException("Error uploading file");
        }
        return multimediaResponseDto;
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
    public MultimediaResponseDto update(Long id,MultipartFile file,MultimediaRequestDto multimedia) {
        var multimediaToUpdate = multimediaMapper.toMultimedia(multimedia,file);
        multimediaToUpdate.setId(id);
        storageService.deleteFile(multimediaToUpdate.getObjectKey());
        var savedData = multimediaRepository.save(multimediaToUpdate);
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
    public Resource downloadMultimedia(Long id) {
        var multimedia = multimediaRepository.findById(id).orElse(null);
        if(multimedia == null){
            throw new RuntimeException("Multimedia not found");
        }
        return new InputStreamResource(storageService.downloadFile(multimedia.getObjectKey()));
    }

}

