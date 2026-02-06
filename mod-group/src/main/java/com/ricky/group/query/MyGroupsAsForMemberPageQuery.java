package com.ricky.group.query;

import com.ricky.common.domain.page.PageQuery;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyGroupsAsForMemberPageQuery extends PageQuery {

    /**
     * 搜索字段，目前支持groupId/name
     */
    @Size(max = 50)
    String search;

    /**
     * 排序字段，目前支持name/createdAt/active
     */
    @Size(max = 50)
    String sortedBy;

    /**
     * 是否按排序字段正向排序，默认逆向排序
     */
    @NotNull
    Boolean ascSort;

}
