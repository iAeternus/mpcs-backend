package com.ricky.file.service;

import com.ricky.file.domain.dto.resp.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse upload(MultipartFile file, String parentId, String path);

}
