package com.memora.memora_backend.storage;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.memora.memora_backend.multimedia.Multimedia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.concurrent.TimeUnit;

@Service
public class StorageServiceImpl implements StorageService {

    @Value("${cloud.bucketName}")
    private String bucketName;

    @Value("${security.jwt.expiration-time}")
    private Long jwtExpiration;

    private final Storage storage;

    public StorageServiceImpl(Storage storage) {
        this.storage = storage;
    }

    /**
     * Upload a file to the storage service
     * @param file the file to upload
     * @param key the key to use for the file
     */
    @Override
    public void uploadFile(byte[] file, String key) {
        BlobId blobId = BlobId.of(bucketName, key);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, file);
    }

    /**
     * Download a file from the storage service
     * @param key the key of the file to download
     * @return the input stream of the file
     */
    public InputStream downloadFile(String key) {
        ReadChannel reader = storage.reader(bucketName, key);
        return Channels.newInputStream(reader);
    }

    /**
     * Delete a file from the storage service
     * @param key the key of the file to delete
     */
    @Override
    public void deleteFile(String key) {
        storage.delete(bucketName, key);
    }

    /**
     * Generate a signed URL for uploading a file
     * @param multimedia the multimedia item to upload
     * @return the signed URL for uploading the file
     */
    @Override
    public String generateSignedUrlForUpload(Multimedia multimedia) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, multimedia.getObjectKey())
                .setContentType(multimedia.getContentType()).build();

        URL url = storage.signUrl(
                blobInfo,
                30, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withContentType()
        );
        return url.toString();
    }

    /**
     * Generate a signed URL for downloading a file
     * @param key the key of the file to download
     * @return the signed URL for downloading the file
     */
    public String generateSignedUrlForDownload(String key) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, key).build();

        // Generate a URL that expires in X minutes based on JWT expiration time
        return storage.signUrl(
                blobInfo,
                jwtExpiration,
                TimeUnit.MILLISECONDS,
                Storage.SignUrlOption.withV4Signature()
        ).toString();
    }
}
