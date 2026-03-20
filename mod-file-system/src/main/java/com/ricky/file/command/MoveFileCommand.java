package com.ricky.file.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveFileCommand implements Command {

    @NotBlank
    @Id(FILE_ID_PREFIX)
    String fileId;

    @NotBlank
    @Id(FOLDER_ID_PREFIX)
    String newParentId;

}
