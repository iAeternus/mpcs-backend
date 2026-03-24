package com.ricky.file;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.permission.PermissionRequired;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.id.custom.CustomId;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.query.*;
import com.ricky.file.service.FileCollabService;
import com.ricky.file.service.FileQueryService;
import com.ricky.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;
import static com.ricky.common.permission.Permission.*;
import static com.ricky.common.permission.ResourceType.FILE;

@Slf4j
@Validated
@CrossOrigin
@RestController
@Tag(name = "文档模块")
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final FileQueryService fileQueryService;
    private final FileCollabService fileCollabService;

    @PutMapping("/{fileId}/name")
    @Operation(summary = "重命名文件")
    @PermissionRequired(value = {MANAGE, WRITE}, resource = "#fileId", resourceType = FILE)
    public void renameFile(@PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
                           @RequestBody @Valid RenameFileCommand command,
                           @AuthenticationPrincipal UserContext userContext) {
        fileService.renameFile(fileId, command, userContext);
    }

    // TODO 逻辑删除

    @Operation(summary = "强制删除文件")
    @DeleteMapping("/{fileId}/delete-force")
    @PermissionRequired(value = MANAGE, resource = "#fileId", resourceType = FILE)
    public void deleteFileForce(@PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
                                @AuthenticationPrincipal UserContext userContext) {
        fileService.deleteFileForce(fileId, userContext);
    }

    @PutMapping("/move")
    @Operation(summary = "移动文件")
    @PermissionRequired(value = MOVE, resource = "#command.fileId", resourceType = FILE)
    public void moveFile(@RequestBody @Valid MoveFileCommand command,
                         @AuthenticationPrincipal UserContext userContext) {
        fileService.moveFile(command, userContext);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{fileId}/download")
    @PermissionRequired(value = READ, resource = "#fileId", resourceType = FILE)
    public ResponseEntity<Resource> download(@PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
                                             @AuthenticationPrincipal UserContext userContext) {
        return fileService.download(fileId, userContext).toDownloadResponse();
    }

    @Operation(summary = "预览文件")
    @GetMapping("/{fileId}/preview")
    @PermissionRequired(value = READ, resource = "#fileId", resourceType = FILE)
    public ResponseEntity<Resource> preview(@PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
                                            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader,
                                            @AuthenticationPrincipal UserContext userContext) {
        DownloadFileResponse fileResponse = fileService.download(fileId, userContext);
        long fileSize = fileResponse.getSize();

        if (rangeHeader == null) {
            return fileResponse.toPreviewResponse();
        }

        return DownloadFileResponse.parseRange(rangeHeader, fileSize)
                .map(rangeInfo -> {
                    DownloadFileResponse rangeResponse = fileService.preview(fileId, userContext, rangeInfo.rangeStart(), rangeInfo.rangeEnd(), fileSize);
                    HttpHeaders headers = DownloadFileResponse.buildRangeHeaders(rangeInfo.rangeStart(), rangeInfo.rangeEnd(), fileSize);
                    return rangeResponse.toPreviewResponse(HttpStatus.PARTIAL_CONTENT, headers);
                })
                .orElseGet(() -> DownloadFileResponse.rangeNotSatisfiable(fileSize));
    }

    @Operation(summary = "获取文件路径")
    @GetMapping("/{customId}/{fileId}/path")
    @PermissionRequired(value = READ, resource = "#fileId", resourceType = FILE)
    public FilePathResponse fetchFilePath(@PathVariable @NotBlank @CustomId String customId,
                                          @PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
                                          @AuthenticationPrincipal UserContext userContext) {
        return fileQueryService.fetchFilePath(customId, fileId, userContext);
    }

    @GetMapping("/{fileId}/info")
    @Operation(summary = "获取文件信息")
    @PermissionRequired(value = READ, resource = "#fileId", resourceType = FILE)
    public FileInfoResponse fetchFileInfo(@PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
                                          @AuthenticationPrincipal UserContext userContext) {
        return fileQueryService.fetchFileInfo(fileId, userContext);
    }

    @PostMapping("/search")
    @Operation(summary = "随处搜索")
    public SearchResponse search(@RequestBody @Valid SearchPageQuery query,
                                 @AuthenticationPrincipal UserContext userContext) {
        return fileQueryService.search(query, userContext);
    }

    @GetMapping("/{fileId}/collab-content")
    @Operation(summary = "获取文件内容用于协同编辑")
    @PermissionRequired(value = WRITE, resource = "#fileId", resourceType = FILE)
    public ResponseEntity<Resource> getCollabContent(
            @PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
            @AuthenticationPrincipal UserContext userContext) {
        log.info("Collaborative edit: fetching content for file[{}]", fileId);
        return fileCollabService.getFileContent(fileId, userContext).toDownloadResponse();
    }

    @PutMapping("/{fileId}/collab-save")
    @Operation(summary = "保存协同编辑后的文件内容")
    @PermissionRequired(value = WRITE, resource = "#fileId", resourceType = FILE)
    public void saveCollabContent(
            @PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
            @RequestParam @Id(FOLDER_ID_PREFIX) String parentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename,
            @AuthenticationPrincipal UserContext userContext) {
        log.info("Collaborative edit: saving content for file[{}]", fileId);
        fileCollabService.saveFileContent(fileId, parentId, file, filename, userContext);
    }

}
