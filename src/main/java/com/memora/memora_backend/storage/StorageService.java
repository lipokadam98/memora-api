package com.memora.memora_backend.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    void uploadFile(MultipartFile file, String key) throws IOException;
    void uploadFile(byte[] file, String key) throws IOException;
    InputStream downloadFile(String key);
    void deleteFile(String key);
    String getDownloadUrl(String key);
}
