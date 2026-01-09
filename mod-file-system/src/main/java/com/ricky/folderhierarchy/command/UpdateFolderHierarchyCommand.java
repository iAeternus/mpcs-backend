package com.ricky.folderhierarchy.command;

import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.custom.CustomId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateFolderHierarchyCommand implements Command {

    /**
     * 文件夹层次结构自定义ID
     */
    @NotBlank
    @CustomId
    String customId;

    @Valid
    @NotNull
    IdTree idTree;

}
