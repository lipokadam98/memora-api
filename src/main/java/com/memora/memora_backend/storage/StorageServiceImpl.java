package com.memora.memora_backend.storage;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.memora.memora_backend.multimedia.Multimedia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.concurrent.TimeUnit;

@Service
public class StorageServiceImpl implements StorageService {

    @Value("${cloud.bucketName}")
    private String bucketName;

    private final Storage storage;

    public StorageServiceImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void uploadFile(byte[] file, String key) throws IOException {
        BlobId blobId = BlobId.of(bucketName, key);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, file);
    }

    public InputStream downloadFile(String key) {
        ReadChannel reader = storage.reader(bucketName, key);
        return Channels.newInputStream(reader);
    }

    @Override
    public void deleteFile(String key) {
        storage.delete(bucketName, key);
    }

    @Override
    public String generateSignedUrl(Multimedia multimedia) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, multimedia.getObjectKey())
                .setContentType(multimedia.getContentType()).build();

        URL url = storage.signUrl(
                blobInfo,
                15, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withContentType()
        );
        return url.toString();
    }
}
