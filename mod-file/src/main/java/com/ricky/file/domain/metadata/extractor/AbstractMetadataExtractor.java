package com.ricky.file.domain.metadata.extractor;

import com.ricky.common.exception.MyException;
import com.ricky.file.domain.metadata.Metadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.ricky.common.exception.ErrorCodeEnum.EXTRACT_METADATA_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.UNSUPPORTED_FILE_TYPE;

public abstract class AbstractMetadataExtractor implements MetadataExtractor {

    @Override
    public Metadata extract(MultipartFile file) {
        if (!supports(file)) {
            throw new MyException(UNSUPPORTED_FILE_TYPE, "Unsupported file type",
                    "mimeType", file.getContentType(), "filename", file.getOriginalFilename());
        }

        try {
            return doExtract(file);
        } catch (IOException e) {
            throw new MyException(EXTRACT_METADATA_FAILED, "Extract metadata failed",
                    "filename", file.getOriginalFilename(), "message", e.getMessage());
        }
    }

    protected abstract Metadata doExtract(MultipartFile file) throws IOException;
}
