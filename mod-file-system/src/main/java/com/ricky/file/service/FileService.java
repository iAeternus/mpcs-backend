package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.query.DownloadFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface FileService {
    void renameFile(String fileId, RenameFileCommand command, UserContext userContext);

    void deleteFileForce(String fileId, UserContext userContext);

    void moveFile(MoveFileCommand command, UserContext userContext);

    DownloadFileResponse download(String fileId, UserContext userContext);

    DownloadFileResponse preview(String fileId, UserContext userContext, long rangeStart, long rangeEnd, long fileSize);
}
