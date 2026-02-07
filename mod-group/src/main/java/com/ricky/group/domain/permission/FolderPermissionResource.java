package com.ricky.group.domain.permission;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderPermissionResource implements PermissionResource {

    String customId;
    String folderId;

}
