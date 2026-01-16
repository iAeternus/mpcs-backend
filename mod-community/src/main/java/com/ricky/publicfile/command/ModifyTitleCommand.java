package com.ricky.publicfile.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.MAX_GENERIC_NAME_LENGTH;
import static com.ricky.common.constants.ConfigConstants.POST_ID_PREFIX;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifyTitleCommand implements Command {

    @NotBlank
    @Id(POST_ID_PREFIX)
    String postId;

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    String newTitle;

}
