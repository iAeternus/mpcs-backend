package com.ricky.file.domain.storage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.FILE_BUCKET;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OssStorageId implements StorageId {

    String bucket;
    String objectKey;

    @Override
    public String getValue() {
        return ""; // TODO
    }

    public static OssStorageId withFileBucket(String objectId) {
        return new OssStorageId(FILE_BUCKET, objectId);
    }
}
