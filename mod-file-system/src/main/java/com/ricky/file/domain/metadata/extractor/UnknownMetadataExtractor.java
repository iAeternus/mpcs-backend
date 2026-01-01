package com.ricky.file.domain.metadata.extractor;

import com.ricky.file.domain.metadata.FileType;
import com.ricky.file.domain.metadata.GeneralMetadata;
import com.ricky.file.domain.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnknownMetadataExtractor extends AbstractMetadataExtractor {

    @Override
    protected Metadata doExtract(MetadataContext ctx) {
        return new GeneralMetadata(
                ctx.getSize(),
                ctx.getMimeType(),
                ctx.getHash(),
                ctx.isMultipart(),
                ctx.getPartCount()
        );
    }

    @Override
    public boolean supports(FileType fileType) {
        return true;
    }
}
