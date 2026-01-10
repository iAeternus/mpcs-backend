package com.ricky.folder.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.id.custom.CustomId;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.MAX_GENERIC_NAME_LENGTH;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateFolderCommand implements Command {

    /**
     * 文件夹层次结构自定义ID
     */
    @NotBlank
    @CustomId
    String customId;

    /**
     * 父文件夹ID
     */
    @Nullable
    @Id(FOLDER_ID_PREFIX)
    String parentId;

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    String folderName;

}
