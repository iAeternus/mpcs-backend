package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.query.DownloadFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileCollabService {

    DownloadFileResponse getFileContent(String fileId, UserContext userContext);

    void saveFileContent(String fileId, String parentId, MultipartFile file, String filename, UserContext userContext);
}
