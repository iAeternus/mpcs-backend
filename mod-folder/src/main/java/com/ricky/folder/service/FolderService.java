package com.ricky.folder.service;

import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.folder.domain.dto.cmd.CreateFolderCommand;
import com.ricky.folder.domain.dto.cmd.RenameFolderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface FolderService {
    String createFolder(CreateFolderCommand command, UserContext userContext);

    void renameFolder(String folderId, RenameFolderCommand command, UserContext userContext);

    void deleteFolder(String folderId, UserContext userContext);
}
