package com.ricky.folder.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folder.domain.dto.cmd.CreateFolderCommand;
import com.ricky.folder.domain.dto.cmd.RenameFolderCommand;

public interface FolderService {
    String createFolder(CreateFolderCommand command, UserContext userContext);

    void renameFolder(String folderId, RenameFolderCommand command, UserContext userContext);

    void deleteFolderForce(String folderId, UserContext userContext);
}
