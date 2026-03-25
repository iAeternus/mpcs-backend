package com.ricky.group.query;

import com.ricky.common.permission.Permission;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderPermissionResponse {

    String folderId;
    String customId;
    Set<Permission> permissions;
    String roleType;
    boolean inherited;
    String memberId;
}
