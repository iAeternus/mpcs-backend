package com.ricky.group.command;

import com.ricky.common.auth.Permission;
import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.collection.NoNullElement;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.*;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddGrantCommand implements Command {

    @NotBlank
    @Id(GROUP_ID_PREFIX)
    String groupId;

    @NotBlank
    @Id(FOLDER_ID_PREFIX)
    String folderId;

    @NotNull
    @NoNullElement
    @Size(max = MAX_GRANT_PERMISSION_SIZE)
    Set<Permission> permissions;

}
