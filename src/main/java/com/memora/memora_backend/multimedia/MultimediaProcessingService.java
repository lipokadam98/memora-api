package com.memora.memora_backend.multimedia;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class MultimediaProcessingService {

    public byte[] createImageThumbnail(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .size(300, 300)
                .outputFormat("jpg")
                .outputQuality(0.7)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    public byte[] createVideoThumbnail(MultipartFile file) throws IOException {
        //TODO Implement video thumbnail creation
        return new byte[0];
    }
}
