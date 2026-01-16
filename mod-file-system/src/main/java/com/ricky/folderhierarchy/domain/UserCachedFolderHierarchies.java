package com.ricky.folderhierarchy.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCachedFolderHierarchies {

    List<UserCachedFolderHierarchy> hierarchies;

}
