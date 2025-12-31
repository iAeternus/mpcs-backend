package com.ricky.folder;

import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.folder.domain.dto.cmd.CreateFolderCommand;
import com.ricky.folder.domain.dto.cmd.RenameFolderCommand;
import com.ricky.folder.service.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@CrossOrigin
@RestController
@Tag(name = "文件夹模块")
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "创建文件夹")
    public IdResponse createFolder(@RequestBody @Valid CreateFolderCommand command,
                                   @AuthenticationPrincipal UserContext userContext) {
        String folderId = folderService.createFolder(command, userContext);
        return IdResponse.returnId(folderId);
    }

    @PostMapping("/{folderId}/name")
    @Operation(summary = "文件夹重命名")
    public void renameFolder(@PathVariable @NotBlank @Id(pre = FOLDER_ID_PREFIX) String folderId,
                             @RequestBody @Valid RenameFolderCommand command,
                             @AuthenticationPrincipal UserContext userContext) {
        folderService.renameFolder(folderId, command, userContext);
    }

    @DeleteMapping("/{folderId}")
    @Operation(summary = "删除文件夹")
    public void deleteFolder(@PathVariable @NotBlank @Id(pre = FOLDER_ID_PREFIX) String folderId,
                             @AuthenticationPrincipal UserContext userContext) {
        folderService.deleteFolder(folderId, userContext);
    }

}
