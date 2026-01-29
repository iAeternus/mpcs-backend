package com.ricky.commenthierarchy.query;

import com.ricky.common.domain.page.PageQuery;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.COMMENT_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.POST_ID_PREFIX;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplyPageQuery extends PageQuery {

    @NotBlank
    @Id(POST_ID_PREFIX)
    String postId; // 发布物ID TODO 如果要优化存储模型，这里要去掉

    @NotBlank
    @Id(COMMENT_ID_PREFIX)
    String commentId; // 父评论ID

    /**
     * 排序字段，目前支持 createdAt
     */
    @Size(max = 50)
    String sortedBy;

    /**
     * 是否按排序字段正向排序，默认逆向排序
     */
    @NotNull
    Boolean ascSort;

}
