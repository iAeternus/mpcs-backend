package com.ricky.folderhierarchy;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.custom.CustomId;
import com.ricky.folderhierarchy.command.UpdateFolderHierarchyCommand;
import com.ricky.folderhierarchy.query.FolderHierarchyResponse;
import com.ricky.folderhierarchy.service.FolderHierarchyQueryService;
import com.ricky.folderhierarchy.service.FolderHierarchyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@CrossOrigin
@RestController
@RequiredArgsConstructor
@Tag(name = "文件夹层级模块")
@RequestMapping("/folder-hierarchy")
public class FolderHierarchyController {

    private final FolderHierarchyService folderHierarchyService;
    private final FolderHierarchyQueryService folderHierarchyQueryService;

    @PutMapping
    @Operation(summary = "更新文件夹层级结构")
    public void updateFolderHierarchy(@RequestBody @Valid UpdateFolderHierarchyCommand command,
                                      @AuthenticationPrincipal UserContext userContext) {
        folderHierarchyService.updateFolderHierarchy(command, userContext);
    }

    @GetMapping("/{customId}")
    @Operation(summary = "获取文件层级结构")
    public FolderHierarchyResponse fetchFolderHierarchy(@PathVariable @NotBlank @CustomId String customId,
                                                        @AuthenticationPrincipal UserContext userContext) {
        return folderHierarchyQueryService.fetchFolderHierarchy(customId, userContext);
    }

}
