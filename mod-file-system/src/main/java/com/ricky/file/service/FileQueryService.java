package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.query.FileInfoResponse;
import com.ricky.file.query.FilePathResponse;

public interface FileQueryService {
    FilePathResponse fetchFilePath(String customId, String fileId, UserContext userContext);

    FileInfoResponse fetchFileInfo(String fileId, UserContext userContext);
}
