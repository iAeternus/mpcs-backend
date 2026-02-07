package com.ricky.group.domain.permission;

import com.ricky.common.domain.marker.ValueObject;

public sealed interface PermissionResource extends ValueObject
        permits FolderPermissionResource, FilePermissionResource {
}
