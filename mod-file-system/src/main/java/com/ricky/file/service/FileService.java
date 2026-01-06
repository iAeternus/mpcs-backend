package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.file.domain.dto.cmd.RenameFileCommand;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public interface FileService {
    void renameFile(String fileId, RenameFileCommand command, UserContext userContext);

    void deleteFileForce(String fileId, UserContext userContext);
}
