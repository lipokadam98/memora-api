package com.memora.memora_backend.multimedia;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/multimedia", produces = MediaType.APPLICATION_JSON_VALUE)
public class MultimediaController {

    private final MultimediaService multimediaService;

    public MultimediaController(MultimediaService multimediaService) {
        this.multimediaService = multimediaService;
    }

    @GetMapping
    public List<Multimedia> getAll(){
        return multimediaService.findAll();
    }

    @GetMapping("/{id}")
    public Multimedia getById(@PathVariable Long id) {
        return multimediaService.findById(id);
    }

    @PostMapping
    public Multimedia create(@RequestBody Multimedia media) {

        /*
            Example to data storage for the multimedia db and cloud data
            String objectKey = "users/" + userId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            // 1. upload to GCS
            storage.create(
                BlobInfo.newBuilder(bucketName, objectKey)
                        .setContentType(file.getContentType())
                        .build(),
                file.getBytes()
            );

            // 2. save metadata to DB
            StoredFile storedFile = new StoredFile();
            storedFile.setOwnerUserId(userId);
            storedFile.setBucketName(bucketName);
            storedFile.setObjectKey(objectKey);
            storedFile.setOriginalFileName(file.getOriginalFilename());
            storedFile.setContentType(file.getContentType());
            storedFile.setSize(file.getSize());
            storedFile.setStatus("READY");
            storedFileRepository.save(storedFile);
        */

        return multimediaService.save(media);
    }

    @PutMapping
    public Multimedia update(@RequestBody Multimedia multimedia) {
        return multimediaService.update(multimedia);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        multimediaService.delete(id);
    }
}
