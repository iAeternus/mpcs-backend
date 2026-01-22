package com.ricky.comment.query;

import com.ricky.common.domain.page.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentPageQuery extends PageQuery {

    /**
     * 排序字段，目前支持TODO
     */
    @Size(max = 50)
    String sortedBy;

}
