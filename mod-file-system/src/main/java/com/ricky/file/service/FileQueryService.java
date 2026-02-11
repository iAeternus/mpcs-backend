package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.query.FileInfoResponse;
import com.ricky.file.query.FilePathResponse;
import com.ricky.file.query.SearchPageQuery;
import com.ricky.file.query.SearchResponse;
import jakarta.validation.Valid;

public interface FileQueryService {
    FilePathResponse fetchFilePath(String customId, String fileId, UserContext userContext);

    FileInfoResponse fetchFileInfo(String fileId, UserContext userContext);

    SearchResponse search(SearchPageQuery query, UserContext userContext);
}
