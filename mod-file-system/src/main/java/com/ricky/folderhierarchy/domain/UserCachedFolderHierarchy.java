package com.ricky.folderhierarchy.domain;

import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.idtree.IdTreeHierarchy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCachedFolderHierarchy {

    String customId;
    IdTree idTree;
    IdTreeHierarchy hierarchy;

}
