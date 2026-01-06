package com.ricky.common.es;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.ricky.common.validation.collection.NoNullElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * 搜索查询参数
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchQuery {

    @NotBlank
    String keyword;

    @NoNullElement
    List<String> fields;

    int page;
    int size;
    boolean highlight;

    @Valid
    HighlightConfig highlightConfig;

    @NoNullElement
    List<SortField> sortFields;
    Map<String, Object> filters;
    BoolQuery.Builder boolQuery;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HighlightConfig {
        Map<String, HighlightField> highlightFields;
        String preTags;
        String postTags;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SortField {
        String field;
        SortOrder order;
    }
}