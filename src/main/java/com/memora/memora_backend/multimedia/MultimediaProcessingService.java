package com.memora.memora_backend.multimedia;

import net.coobird.thumbnailator.Thumbnails;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class MultimediaProcessingService {

    public byte[] createImageThumbnail(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(inputStream)
                .size(300, 300)
                .outputFormat("jpg")
                .outputQuality(0.7)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    public byte[] createVideoThumbnailFromStream(InputStream videoStream) throws IOException {
        if (videoStream == null) {
            throw new IllegalArgumentException("Video stream cannot be null.");
        }

        // 1. Create a temporary path
        // It is important to define the suffix so the OS/JCodec can recognize the video format
        Path tempPath = Files.createTempFile("video-stream-", ".mp4");

        try {
            // 2. Stream the input directly to the temp file
            // StandardCopyOption.REPLACE_EXISTING ensures we don't fail if the file name exists
            Files.copy(videoStream, tempPath, StandardCopyOption.REPLACE_EXISTING);

            // 3. Process the file from disk (as JCodec requires a File/SeekableByteChannel)
            Picture picture = FrameGrab.getFrameFromFile(tempPath.toFile(), 1);
            if (picture == null) {
                throw new IOException("Could not extract frame from video.");
            }

            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
            BufferedImage correctedImage = rotate90Clockwise(bufferedImage);

            // 4. Write to output stream
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(correctedImage, "jpg", baos);
                return baos.toByteArray();
            }

        } catch (Exception e) {
            throw new IOException("Failed to create video thumbnail from stream.", e);
        } finally {
            // 5. Cleanup: Always delete the temporary file, even if an error occurred
            Files.deleteIfExists(tempPath);
        }
    }

    private BufferedImage rotate90Clockwise(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        BufferedImage rotated = new BufferedImage(height, width, source.getType());
        Graphics2D g2d = rotated.createGraphics();
        try {
            AffineTransform transform = new AffineTransform();
            transform.translate(height, 0);
            transform.rotate(Math.toRadians(90));
            g2d.setTransform(transform);
            g2d.drawImage(source, 0, 0, null);
        } finally {
            g2d.dispose();
        }

        return rotated;
    }
}
