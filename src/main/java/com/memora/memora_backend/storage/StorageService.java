package com.memora.memora_backend.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    void uploadFile(MultipartFile file, String key) throws IOException;
    byte[] downloadFile(String key);
    void deleteFile(String key);
}
