package com.ricky.comment.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.COMMENT_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.POST_ID_PREFIX;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteCommentCommand implements Command {

    @NotBlank
    @Id(POST_ID_PREFIX)
    String postId;

    @NotBlank
    @Id(COMMENT_ID_PREFIX)
    String commentId;

}
