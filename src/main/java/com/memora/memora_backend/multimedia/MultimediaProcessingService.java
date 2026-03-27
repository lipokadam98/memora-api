package com.memora.memora_backend.multimedia;

import net.coobird.thumbnailator.Thumbnails;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

    //TODO This is maybe good enough for now, but it has limitations for the rotation, and it can only handle mp4 files
    public byte[] createVideoThumbnail(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file is empty.");
        }

        File tempFile = File.createTempFile("video-upload-", ".mp4");

        try {
            file.transferTo(tempFile);

            Picture picture = FrameGrab.getFrameFromFile(tempFile, 1);
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);

            // Rotate if needed
            BufferedImage correctedImage = rotate90Clockwise(bufferedImage);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(correctedImage, "jpg", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new IOException("Failed to create video thumbnail.", e);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
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
