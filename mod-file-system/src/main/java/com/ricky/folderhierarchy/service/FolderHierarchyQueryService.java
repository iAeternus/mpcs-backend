package com.ricky.folderhierarchy.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folderhierarchy.domain.dto.resp.FolderHierarchyResponse;

public interface FolderHierarchyQueryService {
    FolderHierarchyResponse fetchFolderHierarchy(UserContext userContext);
}
