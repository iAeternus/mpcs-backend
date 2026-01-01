package com.ricky.folderhierarchy.domain;

import com.ricky.folder.domain.FolderRepository;

public interface FolderHierarchyRepository {
    FolderHierarchy byUserId(String userId);

    void save(FolderHierarchy folderHierarchy);

    FolderHierarchy cachedByUserId(String userId);
}
