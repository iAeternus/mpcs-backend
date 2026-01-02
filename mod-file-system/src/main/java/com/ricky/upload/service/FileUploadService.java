package com.ricky.upload.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.upload.domain.dto.resp.FileUploadResponse;
import com.ricky.upload.domain.dto.cmd.CompleteUploadCommand;
import com.ricky.upload.domain.dto.cmd.InitUploadCommand;
import com.ricky.upload.domain.dto.resp.InitUploadResponse;
import com.ricky.upload.domain.dto.resp.UploadChunkResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    FileUploadResponse upload(MultipartFile file, String parentId, UserContext userContext);

    InitUploadResponse initUpload(InitUploadCommand command, UserContext userContext);

    UploadChunkResponse uploadChunk(String uploadId, Integer chunkIndex, MultipartFile chunk, UserContext userContext);

    FileUploadResponse completeUpload(CompleteUploadCommand command, UserContext userContext);

}
