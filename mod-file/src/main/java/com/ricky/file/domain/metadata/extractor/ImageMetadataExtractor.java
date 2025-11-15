package com.ricky.file.domain.metadata.extractor;

import com.ricky.file.domain.metadata.Metadata;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageMetadataExtractor implements MetadataExtractor {
    @Override
    public Metadata extract(MultipartFile file) {
        // TODO
        return null;
    }
}
