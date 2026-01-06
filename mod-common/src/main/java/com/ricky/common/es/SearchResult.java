package com.ricky.common.es;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 搜索结果包装
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchResult<T> {
    List<T> content;
    List<Highlight> highlights;
    long total;
    int page;
    int size;
    int totalPages;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Highlight {
        String id;
        List<FieldHighlight> highlights;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FieldHighlight {
        String fieldName;
        String highlightedText;
    }
}
