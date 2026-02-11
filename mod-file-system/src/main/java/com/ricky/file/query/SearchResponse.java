package com.ricky.file.query;

import com.ricky.common.domain.marker.Response;
import com.ricky.common.es.SearchResult;
import com.ricky.file.domain.es.EsFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchResponse implements Response {

    List<EsFile> content;
    List<SearchResult.Highlight> highlights;
    long total;
    int page;
    int size;
    int totalPages;

    public static SearchResponse from(SearchResult<EsFile> result) {
        return SearchResponse.builder()
                .content(result.getContent())
                .highlights(result.getHighlights())
                .total(result.getTotal())
                .page(result.getPage())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Override
    public String toString() {
        return "SearchResponse{" +
                "content=" + content +
                ", highlights=" + highlights +
                ", total=" + total +
                ", page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                '}';
    }
}
