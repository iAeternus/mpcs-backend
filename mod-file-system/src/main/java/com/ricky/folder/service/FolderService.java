package com.ricky.folder.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folder.command.CreateFolderCommand;
import com.ricky.folder.command.DeleteFolderForceCommand;
import com.ricky.folder.command.RenameFolderCommand;

public interface FolderService {
    String createFolder(CreateFolderCommand command, UserContext userContext);

    void renameFolder(String folderId, RenameFolderCommand command, UserContext userContext);

    void deleteFolderForce(String folderId, DeleteFolderForceCommand command, UserContext userContext);
}
