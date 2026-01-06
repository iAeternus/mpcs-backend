package com.ricky.folderhierarchy.command;

import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.marker.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateFolderHierarchyCommand implements Command {

    @Valid
    @NotNull
    IdTree idTree;

}
