package com.ricky.folder.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderMeta {

    String id;
    String folderName;
    String parentId;
    String path;
    Set<String> fileIds;

}
