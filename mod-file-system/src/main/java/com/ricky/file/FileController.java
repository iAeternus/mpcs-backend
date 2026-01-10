package com.ricky.file;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.id.custom.CustomId;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.query.FetchFilePathResponse;
import com.ricky.file.service.FileQueryService;
import com.ricky.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;

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
    public void renameFile(@PathVariable @NotBlank @Id(FILE_ID_PREFIX) String fileId,
                           @RequestBody @Valid RenameFileCommand command,
                           @AuthenticationPrincipal UserContext userContext) {
        fileService.renameFile(fileId, command, userContext);
    }

    // TODO 逻辑删除

    @Operation(summary = "强制删除文件")
    @DeleteMapping("/{fileId}/delete-force")
    public void deleteFileForce(@PathVariable String fileId,
                                @AuthenticationPrincipal UserContext userContext) {
        fileService.deleteFileForce(fileId, userContext);
    }

    // TODO 移动文件

    @GetMapping("/{customId}/{fileId}/path")
    @Operation(summary = "获取文件路径")
    public FetchFilePathResponse fetchFilePath(@PathVariable @NotBlank @CustomId String customId,
                                               @PathVariable String fileId,
                                               @AuthenticationPrincipal UserContext userContext) {
        return fileQueryService.fetchFilePath(customId, fileId, userContext);
    }

    // TODO 获取文件信息

}
