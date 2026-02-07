package com.ricky.file;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.permission.PermissionRequired;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.id.custom.CustomId;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.query.FileInfoResponse;
import com.ricky.file.query.FilePathResponse;
import com.ricky.file.service.FileQueryService;
import com.ricky.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;
import static com.ricky.common.permission.Permission.*;
import static com.ricky.common.permission.ResourceType.FILE;

@Validated
@CrossOrigin
@RestController
@Tag(name = "文档模块")
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final FileQueryService fileQueryService;

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
        return fileService.download(fileId, userContext).toResponseEntity();
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

}
