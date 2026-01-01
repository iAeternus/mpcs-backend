package com.ricky.file.domain.metadata.extractor;

import com.ricky.file.domain.metadata.FileType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class MetadataExtractorFactory {

    private final Map<FileType, MetadataExtractor> extractors = new EnumMap<>(FileType.class);

    public MetadataExtractorFactory(
            ImageMetadataExtractor img,
            UnknownMetadataExtractor unknown
            // add here...
    ) {
        extractors.put(FileType.IMAGE, img);
        extractors.put(FileType.UNKNOWN, unknown);
    }

    public MetadataExtractor getExtractor(FileType fileType) {
        return extractors.getOrDefault(fileType, extractors.get(FileType.UNKNOWN));
    }

    public MetadataExtractor getExtractorByFilename(String filename) {
        return null; // TODO
    }

}
