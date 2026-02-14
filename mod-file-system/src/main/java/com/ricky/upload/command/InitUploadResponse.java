package com.ricky.upload.command;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InitUploadResponse implements Response {

    boolean uploaded; // 是否秒传 true=是 false=否
    String fileId; // 存储ID，uploaded=true时返回，否则返回null
    String storageId; // 存储ID，uploaded=true时返回，否则返回null
    String uploadId; // 分片上传 ID，uploaded=false时返回，否则返回null
    Set<Integer> uploadedChunks; // 已上传分片（断点），uploaded=false时返回，否则返回null

    public static InitUploadResponse fastUploaded(String fileId, String storageId) {
        return InitUploadResponse.builder()
                .uploaded(true)
                .fileId(fileId)
                .storageId(storageId)
                .build();
    }

    public static InitUploadResponse notFastUploaded(String uploadId, Set<Integer> uploadedChunks) {
        return InitUploadResponse.builder()
                .uploaded(false)
                .uploadId(uploadId)
                .uploadedChunks(uploadedChunks)
                .build();
    }

}
