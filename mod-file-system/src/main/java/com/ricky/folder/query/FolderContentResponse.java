package com.ricky.folder.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderContentResponse implements Response {

    List<Folder> folders;
    List<File> files;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class File {
        String id;
        String filename;
        LocalDateTime lastModifiedAt;
        Long size;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Folder {
        String id;
        String folderName;
        LocalDateTime lastModifiedAt;
    }

}
