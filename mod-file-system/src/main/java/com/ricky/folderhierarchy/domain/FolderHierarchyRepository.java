package com.ricky.folderhierarchy.domain;

public interface FolderHierarchyRepository {
    FolderHierarchy byUserId(String userId);

    void save(FolderHierarchy folderHierarchy);

    FolderHierarchy cachedByUserId(String userId);
}
