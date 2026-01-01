package com.ricky.file.domain.metadata.extractor;

import com.ricky.common.exception.MyException;
import com.ricky.file.domain.metadata.Metadata;

import static com.ricky.common.exception.ErrorCodeEnum.EXTRACT_METADATA_FAILED;

public abstract class AbstractMetadataExtractor implements MetadataExtractor {

    @Override
    public Metadata extract(MetadataContext ctx) {
        try {
            return doExtract(ctx);
        } catch (Exception e) {
            throw new MyException(EXTRACT_METADATA_FAILED, "Extract metadata failed",
                    "filename", ctx.getFilename(), "message", e.getMessage()
            );
        }
    }

    protected abstract Metadata doExtract(MetadataContext ctx) throws Exception;

}
