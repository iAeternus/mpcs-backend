package com.ricky.folder.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folder.query.FolderContentResponse;

public interface FolderQueryService {
    FolderContentResponse fetchFolderContent(String customId, String folderId, UserContext userContext);
}
