package com.ricky.group.domain.permission;

import com.ricky.common.permission.Permission;
import com.ricky.common.permission.ResourceType;
import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionMetadata implements ValueObject {

    Set<Permission> required;
    String resourceSpel;
    ResourceType resourceType;
    boolean batch;

}