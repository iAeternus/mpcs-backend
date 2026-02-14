package com.ricky.file.domain.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.FILE_BUCKET;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OssStorageId implements StorageId {

    @NotBlank
    String bucket;

    @NotBlank
    String objectKey;

    @Override
    public String getValue() {
        return bucket + "\\" + objectKey;
    }

    public static OssStorageId withFileBucket(String objectId) {
        return new OssStorageId(FILE_BUCKET, objectId);
    }
}
