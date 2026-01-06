package com.ricky.common.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;
import com.ricky.file.domain.EsFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ricky.common.constants.ConfigConstants.FILE_ES_INDEX_NAME;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static com.ricky.common.utils.ValidationUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileElasticSearchService implements ElasticSearchService<EsFile> {

    private final ElasticsearchClient esClient;

    @Override
    public IndexResponse index(EsFile file) {
        validateEsFile(file);

        try {
            IndexResponse response = esClient.index(i -> i
                    .index(FILE_ES_INDEX_NAME)
                    .id(file.getId())
                    .document(file)
            );

            log.debug("文件索引成功: ID={}, Version={}", response.id(), response.version());
            return response;
        } catch (IOException e) {
            throw new MyException(ES_DOCUMENT_INDEX_ERROR,
                    "文件索引失败", "fileId", file.getId(), "exception", e);
        }
    }

    @Override
    public BulkResponse indexBulk(List<EsFile> files) {
        if (isEmpty(files)) {
            log.warn("批量索引文件列表为空");
            return null;
        }

        BulkRequest.Builder br = new BulkRequest.Builder();

        for (EsFile file : files) {
            validateEsFile(file);
            br.operations(op -> op
                    .index(idx -> idx
                            .index(FILE_ES_INDEX_NAME)
                            .id(file.getId())
                            .document(file)
                    )
            );
        }

        try {
            BulkResponse response = esClient.bulk(br.build());

            if (response.errors()) {
                log.error("批量索引存在错误，共{}个文件，失败{}个",
                        files.size(),
                        response.items().stream().filter(item -> item.error() != null).count());

                // 记录具体错误
                response.items().stream()
                        .filter(item -> item.error() != null)
                        .forEach(item -> log.error("批量索引错误项: ID={}, 原因={}",
                                item.id(), item.error().reason()));
            } else {
                log.info("批量索引成功: 文件数={}, 耗时={}ms",
                        files.size(), response.took());
            }

            return response;
        } catch (IOException e) {
            throw new MyException(ES_DOCUMENT_INDEX_ERROR,
                    "批量索引失败", "fileCount", files.size(), "exception", e);
        }
    }

    @Override
    public SearchResult<EsFile> search(String keyword, int page, int size) {
        if (page < 0) {
            throw new MyException(PARAMS_ERROR, "页码不能为负数");
        }
        if (size <= 0 || size > 100) {
            throw new MyException(PARAMS_ERROR, "每页大小必须在1-100之间");
        }

        try {
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                    .index(FILE_ES_INDEX_NAME)
                    .from(page * size)
                    .size(size);

            // 构建查询条件
            if (StringUtils.hasText(keyword)) {
                // 多字段搜索
                BoolQuery.Builder boolQuery = new BoolQuery.Builder();

                // 文件名：前缀匹配和高精度匹配
                boolQuery.should(s -> s
                        .matchPhrasePrefix(m -> m
                                .field("name")
                                .query(keyword)
                                .boost(3.0f)
                        )
                ).should(s -> s
                        .match(m -> m
                                .field("name")
                                .query(keyword)
                                .analyzer("ik_smart")
                                .boost(2.0f)
                        )
                );

                // 摘要：模糊匹配
                boolQuery.should(s -> s
                        .match(m -> m
                                .field("summary")
                                .query(keyword)
                                .analyzer("ik_smart")
                                .boost(1.5f)
                                .fuzziness("1")
                        )
                );

                // 关键词和分类：精确匹配
                boolQuery.should(s -> s
                        .match(m -> m
                                .field("keywords")
                                .query(keyword)
                                .boost(1.0f)
                        )
                ).should(s -> s
                        .match(m -> m
                                .field("category")
                                .query(keyword)
                                .boost(0.5f)
                        )
                );

                boolQuery.minimumShouldMatch("1");
                requestBuilder.query(q -> q.bool(boolQuery.build()));

                // 高亮设置
                requestBuilder.highlight(h -> h
                        .fields("name", f -> f
                                .preTags("<em class=\"highlight\">")
                                .postTags("</em>")
                        )
                        .fields("summary", f -> f
                                .preTags("<em class=\"highlight\">")
                                .postTags("</em>")
                                .numberOfFragments(2)
                                .fragmentSize(150)
                        )
                );

            } else {
                // 无关键词时查询所有
                requestBuilder.query(q -> q.matchAll(m -> m));
            }

            // 按最后修改时间倒序排序
            requestBuilder.sort(s -> s.field(f -> f
                    .field("lastModified")
                    .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)
            ));

            // 执行搜索
            SearchResponse<EsFile> response = esClient.search(
                    requestBuilder.build(), EsFile.class);

            return buildSearchResult(response, page, size);
        } catch (IOException e) {
            throw new MyException(ES_SEARCH_ERROR, "搜索文件失败",
                    "keyword", keyword, "page", page, "size", size, "exception", e);
        }
    }

    @Override
    public DeleteResponse deleteById(String id) {
        if (isBlank(id)) {
            throw new MyException(PARAMS_ERROR, "文件ID不能为空");
        }

        try {
            DeleteResponse response = esClient.delete(d -> d
                    .index(FILE_ES_INDEX_NAME)
                    .id(id)
            );

            log.debug("删除文件成功: ID={}", id);
            return response;
        } catch (IOException e) {
            throw new MyException(ES_DOCUMENT_DELETE_ERROR,
                    "删除文件失败", "fileId", id, "exception", e);
        }
    }

    @Override
    public BulkResponse deleteBulk(List<String> ids) {
        if (isEmpty(ids)) {
            log.warn("批量删除ID列表为空");
            return null;
        }

        BulkRequest.Builder br = new BulkRequest.Builder();
        ids.forEach(id -> {
            if (StringUtils.hasText(id)) {
                br.operations(op -> op
                        .delete(d -> d
                                .index(FILE_ES_INDEX_NAME)
                                .id(id)
                        )
                );
            }
        });

        try {
            BulkResponse response = esClient.bulk(br.build());

            log.info("批量删除完成: 请求数={}, 成功数={}",
                    ids.size(),
                    response.items().stream().filter(i -> i.error() == null).count());
            return response;
        } catch (IOException e) {
            throw new MyException(ErrorCodeEnum.ES_DOCUMENT_DELETE_ERROR,
                    "批量删除失败", "idCount", ids.size(), "exception", e);
        }
    }

    @Override
    public EsFile byId(String id) {
        if (isBlank(id)) {
            throw new MyException(PARAMS_ERROR, "文件ID不能为空");
        }

        try {
            GetResponse<EsFile> response = esClient.get(g -> g
                    .index(FILE_ES_INDEX_NAME)
                    .id(id), EsFile.class);

            if (response.found()) {
                return response.source();
            }

            log.debug("文件未找到: ID={}", id);
            return null;

        } catch (IOException e) {
            throw new MyException(ES_SEARCH_ERROR, "查询文件失败", "fileId", id, "exception", e);
        }
    }

    /**
     * 构建搜索结果
     */
    private SearchResult<EsFile> buildSearchResult(SearchResponse<EsFile> response, int page, int size) {
        TotalHits totalHits = response.hits().total();
        long total = totalHits != null ? totalHits.value() : 0;
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        List<EsFile> files = new ArrayList<>();
        List<SearchResult.Highlight> highlights = new ArrayList<>();

        for (Hit<EsFile> hit : response.hits().hits()) {
            if (hit.source() != null) {
                files.add(hit.source());

                // 提取高亮信息
                if (hit.highlight() != null && !hit.highlight().isEmpty()) {
                    SearchResult.Highlight highlight = SearchResult.Highlight.builder()
                            .id(hit.id())
                            .highlights(new ArrayList<>())
                            .build();

                    hit.highlight().forEach((field, highlightTexts) ->
                            highlightTexts.forEach(text ->
                                    highlight.getHighlights().add(
                                            SearchResult.FieldHighlight.builder()
                                                    .fieldName(field)
                                                    .highlightedText(text)
                                                    .build()
                                    )
                            ));

                    highlights.add(highlight);
                }
            }
        }

        return SearchResult.<EsFile>builder()
                .content(files)
                .highlights(highlights)
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 验证文件对象
     */
    private void validateEsFile(EsFile file) {
        requireNonNull(file, "EsFile must not be null");
        file.validate();
    }
}