package com.ricky.group.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.custom.CustomId;
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
public class CreateGroupCommand implements Command {

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    String name;

    @NotBlank
    @CustomId
    String customId;

}
