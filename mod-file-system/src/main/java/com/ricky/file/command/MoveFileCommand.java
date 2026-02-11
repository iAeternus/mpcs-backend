package com.ricky.file.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.exception.MyException;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;
import static com.ricky.common.exception.ErrorCodeEnum.CANNOT_MOVE_FILE_TO_ROOT;
import static com.ricky.common.utils.ValidationUtils.isBlank;

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
