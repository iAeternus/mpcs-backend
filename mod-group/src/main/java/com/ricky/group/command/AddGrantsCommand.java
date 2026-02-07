package com.ricky.group.command;

import com.ricky.common.permission.Permission;
import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.collection.NoNullElement;
import com.ricky.common.validation.id.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.*;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddGrantsCommand implements Command {

    @NotBlank
    @Id(GROUP_ID_PREFIX)
    String groupId;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_GROUP_FOLDER_SIZE)
    List<@Id(FOLDER_ID_PREFIX) String> folderIds;

    @NotNull
    @NoNullElement
    @Size(max = MAX_GRANT_PERMISSION_SIZE)
    Set<Permission> permissions;

}
