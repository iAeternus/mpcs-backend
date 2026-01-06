package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.query.FetchFilePathResponse;

public interface FileQueryService {
    FetchFilePathResponse fetchFilePath(String fileId, UserContext userContext);
}
