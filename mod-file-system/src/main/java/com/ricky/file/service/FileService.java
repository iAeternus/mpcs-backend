package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.query.DownloadFileResponse;
import jakarta.validation.constraints.NotBlank;

public interface FileService {
    void renameFile(String fileId, RenameFileCommand command, UserContext userContext);

    void deleteFileForce(String fileId, UserContext userContext);

    void moveFile(MoveFileCommand command, UserContext userContext);

    DownloadFileResponse download(String fileId, UserContext userContext);
}
