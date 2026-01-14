package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import jakarta.validation.Valid;

public interface FileService {
    void renameFile(String fileId, RenameFileCommand command, UserContext userContext);

    void deleteFileForce(String fileId, UserContext userContext);

    void moveFile(MoveFileCommand command, UserContext userContext);
}
