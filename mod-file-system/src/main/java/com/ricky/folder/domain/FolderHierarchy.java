package com.ricky.folder.domain;

import com.ricky.common.domain.idtree.IdTree;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderHierarchy {

    String customId;
    List<FolderMeta> folders;

    public IdTree buildIdTree() {
        IdTree idTree = new IdTree(new ArrayList<>(0));
        folders.forEach(folder -> idTree.addNode(folder.getParentId(), folder.getId()));
        return idTree;
    }

}
