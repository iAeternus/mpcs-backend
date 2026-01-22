package com.ricky.commenthierarchy.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.*;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplyCommand implements Command {

    @NotBlank
    @Id(POST_ID_PREFIX)
    String postId;

    @NotBlank
    @Id(COMMENT_ID_PREFIX)
    String parentId;

    @NotBlank
    @Size(max = MAX_GENERIC_TEXT_LENGTH)
    String content;

}
