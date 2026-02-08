package com.ricky.file.domain.storage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoredFile {
    StorageId storageId;
    String hash;
    long size;
}
