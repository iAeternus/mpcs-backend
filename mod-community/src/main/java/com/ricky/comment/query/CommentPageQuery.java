package com.ricky.comment.query;

import com.ricky.common.domain.page.PageQuery;
import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import static com.ricky.common.constants.ConfigConstants.POST_ID_PREFIX;

@Value
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentPageQuery extends PageQuery {

    @NotNull
    @Id(POST_ID_PREFIX)
    String postId;

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
