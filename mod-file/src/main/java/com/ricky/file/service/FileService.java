package com.ricky.file.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.domain.dto.resp.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse upload(MultipartFile file, String parentId, String path, UserContext userContext);

}
