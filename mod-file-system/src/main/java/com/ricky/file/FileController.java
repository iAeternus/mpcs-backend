package com.ricky.file;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.file.domain.dto.cmd.RenameFileCommand;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;
import com.ricky.file.service.FileQueryService;
import com.ricky.file.service.FileService;
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
    public void renameFile(@PathVariable @NotBlank @Id(pre = FILE_ID_PREFIX) String fileId,
                           @RequestBody @Valid RenameFileCommand command,
                           @AuthenticationPrincipal UserContext userContext) {
        fileService.renameFile(fileId, command, userContext);
    }

    // TODO 逻辑删除

    @DeleteMapping("/{fileId}/delete-force")
    public void deleteFileForce(@PathVariable String fileId,
                                @AuthenticationPrincipal UserContext userContext) {
        fileService.deleteFileForce(fileId, userContext);
    }

    // TODO 移动文件

    @GetMapping("/{fileId}/path")
    public FetchFilePathResponse fetchFilePath(@PathVariable String fileId,
                                               @AuthenticationPrincipal UserContext userContext) {
        return fileQueryService.fetchFilePath(fileId, userContext);
    }

    // TODO 获取文件信息

}
