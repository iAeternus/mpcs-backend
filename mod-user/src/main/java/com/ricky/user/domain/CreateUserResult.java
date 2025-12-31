package com.ricky.user.domain;

import com.ricky.folderhierarchy.domain.FolderHierarchy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateUserResult {

    User user;
    FolderHierarchy folderHierarchy;

}
