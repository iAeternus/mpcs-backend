package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;

public interface FileService {
    void deleteFileForce(String fileId, UserContext userContext);

    FetchFilePathResponse fetchFilePath(String fileId, UserContext userContext);
}
