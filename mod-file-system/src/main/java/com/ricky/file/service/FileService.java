package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.command.RenameFileCommand;

public interface FileService {
    void renameFile(String fileId, RenameFileCommand command, UserContext userContext);

    void deleteFileForce(String fileId, UserContext userContext);
}
