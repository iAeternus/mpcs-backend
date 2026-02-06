package com.ricky.folder.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folder.query.FolderContentResponse;
import com.ricky.folder.query.FolderHierarchyResponse;

public interface FolderQueryService {
    FolderContentResponse fetchFolderContent(String customId, String folderId, UserContext userContext);

    FolderHierarchyResponse fetchFolderHierarchy(String customId, UserContext userContext);
}
