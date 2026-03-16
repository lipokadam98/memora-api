package com.memora.memora_backend.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class StorageServiceImpl implements StorageService {

    @Value("${cloud.bucketName}")
    private String bucketName;


    private final Storage storage;

    public StorageServiceImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void uploadFile(MultipartFile file, String key) throws IOException {
        BlobId blobId = BlobId.of(bucketName, key);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, file.getBytes());
    }

    @Override
    public byte[] downloadFile(String key) {
        return storage.readAllBytes(bucketName, key);
    }

    @Override
    public void deleteFile(String key) {

    }
}
