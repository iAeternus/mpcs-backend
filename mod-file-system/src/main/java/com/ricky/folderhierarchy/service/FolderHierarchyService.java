package com.ricky.folderhierarchy.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folderhierarchy.command.UpdateFolderHierarchyCommand;

public interface FolderHierarchyService {
    void updateFolderHierarchy(UpdateFolderHierarchyCommand command, UserContext userContext);
}
