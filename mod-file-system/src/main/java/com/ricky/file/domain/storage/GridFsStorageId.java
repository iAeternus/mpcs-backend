package com.ricky.file.domain.storage;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.bson.types.ObjectId;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GridFsStorageId implements StorageId {

    @NotNull
    String value;

    public ObjectId toObjectId() {
        return new ObjectId(value);
    }

}
