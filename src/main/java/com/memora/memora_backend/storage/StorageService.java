package com.memora.memora_backend.storage;

import com.memora.memora_backend.multimedia.Multimedia;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    void uploadFile(byte[] file, String key) throws IOException;
    InputStream downloadFile(String key);
    void deleteFile(String key);
    String generateSignedUrl(Multimedia multimedia);
}
