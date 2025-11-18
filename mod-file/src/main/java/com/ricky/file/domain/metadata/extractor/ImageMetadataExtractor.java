package com.ricky.file.domain.metadata.extractor;

import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.utils.ChecksumUtils;
import com.ricky.file.domain.metadata.ImageMetadata;
import com.ricky.file.domain.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_READ_FAILED;

@Component
@RequiredArgsConstructor
public class ImageMetadataExtractor extends AbstractMetadataExtractor {

    private final FileHasherFactory fileHasherFactory;

    @Override
    protected Metadata doExtract(MultipartFile file) throws IOException {
        long size = file.getSize();
        String mimeType = file.getContentType();
        String hash = fileHasherFactory.getFileHasher().hash(file.getInputStream());
        long checksum = ChecksumUtils.crc32(file.getInputStream());
        Dimension dimension = extractImageDimension(file);
        return new ImageMetadata(size, mimeType, hash, checksum, dimension.getWidth(), dimension.getHeight());
    }

    private Dimension extractImageDimension(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new MyException(FILE_READ_FAILED, "The image file cannot be read. It may not be a valid image format",
                    "mimeType", file.getContentType(), "filename", file.getOriginalFilename());
        }
        return new Dimension(image.getWidth(), image.getHeight());
    }

    // TODO 添加校验扩展名的逻辑
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && CONTENT_TYPES.contains(contentType);
    }

    // TODO 拿出去
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }


    @Value
    private static class Dimension {
        int width;
        int height;
    }
}
