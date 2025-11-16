package com.ricky.file.domain.metadata.extractor;

import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.utils.ChecksumUtils;
import com.ricky.file.domain.metadata.GeneralMetadata;
import com.ricky.file.domain.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UnknownMetadataExtractor extends AbstractMetadataExtractor {

    private final FileHasherFactory fileHasherFactory;

    @Override
    protected Metadata doExtract(MultipartFile file) throws IOException {
        long size = file.getSize();
        String mimeType = file.getContentType();
        String hash = fileHasherFactory.getFileHasher().hash(file.getInputStream());
        long checksum = ChecksumUtils.crc32(file.getInputStream());
        return new GeneralMetadata(size, mimeType, hash, checksum);
    }

    @Override
    public boolean supports(MultipartFile file) {
        return true;
    }
}
