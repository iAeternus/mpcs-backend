package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;

public interface FileQueryService {
    FetchFilePathResponse fetchFilePath(String fileId, UserContext userContext);
}
