package com.ricky.folder.query;

import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderHierarchyResponse implements Response {

    IdTree idTree;
    List<HierarchyFolder> allFolders;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HierarchyFolder {
        String id;
        String folderName;
        String parentId;
        String path;
    }

}
