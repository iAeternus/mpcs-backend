package com.ricky.upload;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.index.NonNegIndex;
import com.ricky.common.validation.path.Path;
import com.ricky.upload.domain.dto.resp.FileUploadResponse;
import com.ricky.upload.domain.dto.cmd.CompleteUploadCommand;
import com.ricky.upload.domain.dto.cmd.InitUploadCommand;
import com.ricky.upload.domain.dto.resp.InitUploadResponse;
import com.ricky.upload.domain.dto.resp.UploadChunkResponse;
import com.ricky.upload.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.UPLOAD_SESSION_ID_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Validated
@CrossOrigin
@RestController
@Tag(name = "文件上传模块")
@RequiredArgsConstructor
@RequestMapping("/files/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @ResponseStatus(CREATED)
    @Operation(summary = "普通上传")
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse upload(@RequestParam("file") @NotNull MultipartFile file,
                                     @RequestParam("parentId") @Id(pre = FOLDER_ID_PREFIX) @NotBlank String parentId,
                                     @AuthenticationPrincipal UserContext userContext) {
        return fileUploadService.upload(file, parentId, userContext);
    }

    @PostMapping("/init")
    @Operation(summary = "初始化分片上传")
    public InitUploadResponse initUpload(@RequestBody @Valid InitUploadCommand command,
                                         @AuthenticationPrincipal UserContext userContext) {
        return fileUploadService.initUpload(command, userContext);
    }

    @Operation(summary = "上传分片")
    @PostMapping(value = "/chunk", consumes = MULTIPART_FORM_DATA_VALUE)
    public UploadChunkResponse uploadChunk(@RequestParam @Id(pre = UPLOAD_SESSION_ID_PREFIX) @NotBlank String uploadId,
                                           @RequestParam @NotNull @NonNegIndex Integer chunkIndex,
                                           @RequestParam @NotNull MultipartFile chunk,
                                           @AuthenticationPrincipal UserContext userContext) {
        return fileUploadService.uploadChunk(uploadId, chunkIndex, chunk, userContext);
    }

    /**
     * 秒传是上传流程优化，而不是文件创建的捷径。
     * File 聚合根只在 completeUpload 阶段创建
     */
    @ResponseStatus(CREATED)
    @Operation(summary = "分片上传完成")
    @PostMapping("/complete")
    public FileUploadResponse completeUpload(@RequestBody @Valid CompleteUploadCommand command,
                                             @AuthenticationPrincipal UserContext userContext) {
        return fileUploadService.completeUpload(command, userContext);
    }

}
