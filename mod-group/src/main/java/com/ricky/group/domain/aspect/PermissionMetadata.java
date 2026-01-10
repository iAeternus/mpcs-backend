package com.ricky.group.domain.aspect;

import com.ricky.common.auth.Permission;

import java.util.Set;

public record PermissionMetadata(
        Set<Permission> required,
        String[] resourceSpels,
        boolean batch
) {
}
