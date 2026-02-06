package com.ricky.common.domain.page;

import org.springframework.data.mongodb.core.query.Criteria;

@FunctionalInterface
public interface SearchCriteriaBuilder<Q extends PageQuery> {

    /**
     * @param search  search 字符串
     * @param base    当前 Criteria（可继续 and/or）
     * @param query   原始 PageQuery
     */
    Criteria build(String search, Criteria base, Q query);
}
