package com.ricky.folder.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.custom.CustomId;
import com.ricky.folder.query.FolderContentResponse;
import com.ricky.folder.query.FolderHierarchyResponse;
import jakarta.validation.constraints.NotBlank;

public interface FolderQueryService {
    FolderContentResponse fetchFolderContent(String customId, String folderId, UserContext userContext);

    FolderHierarchyResponse fetchFolderHierarchy(String customId, UserContext userContext);
}
