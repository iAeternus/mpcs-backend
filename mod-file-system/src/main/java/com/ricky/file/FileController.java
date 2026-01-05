package com.ricky.file;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;
import com.ricky.file.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@CrossOrigin
@RestController
@Tag(name = "文档模块")
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    // TODO 重命名、逻辑删除、强制删除、移动文件、获取文件信息

    @DeleteMapping("/{fileId}/delete-force")
    public void deleteFileForce(@PathVariable String fileId,
                                @AuthenticationPrincipal UserContext userContext) {
        fileService.deleteFileForce(fileId, userContext);
    }

    @GetMapping("/{fileId}/path")
    public FetchFilePathResponse fetchFilePath(@PathVariable String fileId,
                                               @AuthenticationPrincipal UserContext userContext) {
        return fileService.fetchFilePath(fileId, userContext);
    }

}
