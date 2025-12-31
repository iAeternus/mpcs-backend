package com.ricky.folder.domain.dto.cmd;

import com.ricky.common.domain.marker.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.MAX_GENERIC_NAME_LENGTH;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenameFolderCommand implements Command {

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    String newName;

}
