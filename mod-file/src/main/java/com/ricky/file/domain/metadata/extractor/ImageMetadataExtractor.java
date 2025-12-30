package com.ricky.file.domain.metadata.extractor;

import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.file.domain.metadata.FileType;
import com.ricky.file.domain.metadata.ImageMetadata;
import com.ricky.file.domain.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_READ_FAILED;
import static com.ricky.common.utils.ValidationUtils.isNull;

@Component
@RequiredArgsConstructor
public class ImageMetadataExtractor extends AbstractMetadataExtractor {

    private final FileHasherFactory fileHasherFactory;

    @Override
    protected Metadata doExtract(MetadataContext ctx) throws Exception {
        if (!ctx.hasStream()) {
            throw new MyException(FILE_READ_FAILED, "Image metadata requires inputStream");
        }

        BufferedImage image = ImageIO.read(ctx.getInputStream());
        if (isNull(image)) {
            throw new MyException(FILE_READ_FAILED, "Invalid image file");
        }

        return new ImageMetadata(
                ctx.getSize(),
                ctx.getMimeType(),
                ctx.getHash(),
                ctx.isMultipart(),
                ctx.getPartCount(),
                image.getWidth(),
                image.getHeight()
        );
    }

    @Override
    public boolean supports(FileType fileType) {
        return fileType == FileType.IMAGE; // TODO 添加校验扩展名的逻辑
    }

}
