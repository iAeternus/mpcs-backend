package com.ricky.folder.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.id.custom.CustomId;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveFolderCommand implements Command {

    @NotBlank
    @CustomId
    String customId;

    @NotBlank
    @Id(FOLDER_ID_PREFIX)
    String folderId;

    /**
     * 新父文件夹ID，若空则为根目录
     */
    @Nullable
    @Id(FOLDER_ID_PREFIX)
    String newParentId;

}
