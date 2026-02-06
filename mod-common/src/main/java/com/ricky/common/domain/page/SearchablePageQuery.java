package com.ricky.common.domain.page;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SearchablePageQuery extends PageQuery {

    /**
     * 搜索字段，字段值
     * 派生类必须注释说明支持的字段
     */
    protected String search;

    /**
     * 排序字段。字段名
     * 派生类必须注释说明支持的字段
     */
    @Size(max = 50)
    protected String sortedBy;

    /**
     * 是否按排序字段正向排序，默认逆向排序
     */
    protected Boolean ascSort = false;

    public boolean asc() {
        return Boolean.TRUE.equals(ascSort);
    }
}
