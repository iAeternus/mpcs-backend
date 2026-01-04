package com.ricky.common.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.ricky.file.domain.EsFile;
import com.ricky.file.domain.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.constants.ConfigConstants.FILE_ES_INDEX_NAME;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileElasticSearchService implements ElasticSearchService<EsFile> {

    private final ElasticsearchClient esClient;

    @Override
    public void upload(EsFile esFile) {
        checkAndCreateIndex();

        IndexResponse resp = index(IndexRequest.of(b -> b
                .index(FILE_ES_INDEX_NAME)
                .id(esFile.getId())
                .document(esFile)
        ));

        log.info("文件索引成功: ID={}, Version={}, Result={}", resp.id(), resp.version(), resp.result());
    }

    @Override
    public void uploadBatch(List<EsFile> objs) {
        // TODO
    }

    @Override
    public List<EsFile> pageSearch(String keyword, int page, int size, boolean highlight) {
        if (isBlank(keyword)) {
            return List.of();
        }

        try {
            // 构建搜索请求
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                    .index(FILE_ES_INDEX_NAME)
                    .from(page * size)
                    .size(size);

            // 构建查询条件，多字段匹配，类似IDEA的全局搜索
            var boolQuery = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();

            // 在多个字段中进行模糊搜索
            boolQuery.should(s -> s
                    .matchPhrasePrefix(m -> m  // 前缀匹配，支持自动补全
                            .field("name")
                            .query(keyword)
                            .boost(3.0f)  // 文件名匹配权重最高
                    )
            ).should(s -> s
                    .match(m -> m
                            .field("name")
                            .query(keyword)
                            .boost(2.0f)
                    )
            ).should(s -> s
                    .match(m -> m
                            .field("summary")
                            .query(keyword)
                            .boost(1.5f)
                    )
            ).should(s -> s
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
            ).minimumShouldMatch("1");  // 至少一个should条件匹配

            requestBuilder.query(q -> q.bool(boolQuery.build()));

            // 按lastModified倒序排序
            requestBuilder.sort(s -> s
                    .field(f -> f
                            .field("lastModified")
                            .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                    )
            );

            // 设置高亮显示
            if (highlight) {
                requestBuilder.highlight(h -> h
                        .fields("name", f -> f
                                .preTags("<span class=\"highlight\">")
                                .postTags("</span>")
                        )
                        .fields("summary", f -> f
                                .preTags("<span class=\"highlight\">")
                                .postTags("</span>")
                        )
                );
            }

            // 投影
            requestBuilder.source(s -> s
                    .filter(f -> f
                            .includes("id", "name", "category", "summary",
                                    "keywords", "sizeInBytes", "lastModified")
                    )
            );

            // 执行搜索
            SearchRequest request = requestBuilder.build();
            SearchResponse<EsFile> response = esClient.search(request, EsFile.class);

            // 处理搜索结果
            TotalHits total = response.hits().total();
            log.debug("双Shift搜索完成: keyword={}, 总结果数={}, 返回数量={}",
                    keyword, total != null ? total.value() : 0, response.hits().hits().size());

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(toImmutableList());
        } catch (IOException e) {
            log.error("全局搜索失败: keyword={}", keyword, e);
            return List.of();
        }
    }

    @Override
    public void removeById(String id) {
        // TODO
    }

    private void checkAndCreateIndex() {
        boolean exists = indexExists();
        if (!exists) {
            createIndex();
        }
    }

    private boolean indexExists() {
        try {
            return esClient.indices()
                    .exists(e -> e.index(FILE_ES_INDEX_NAME))
                    .value();
        } catch (IOException ex) {
            log.error("检查ES索引[{}]失败: {}", FILE_ES_INDEX_NAME, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void createIndex() {
        try {
            // 索引
            esClient.indices().create(c -> c
                    .index(FILE_ES_INDEX_NAME)
                    .settings(s -> s
                            .numberOfShards("3")
                            .numberOfReplicas("1")
                            .analysis(a -> a
                                    .analyzer("ik_filename", analyzer -> analyzer
                                            .custom(custom -> custom
                                                    .tokenizer("ik_max_word")
                                                    .filter("lowercase")
                                            )
                                    )
                            )
                    )
            );

            // 映射
            esClient.indices().putMapping(p -> p
                    .index(FILE_ES_INDEX_NAME)
                    .properties("name", prop -> prop
                            .text(t -> t
                                    .analyzer("ik_filename")
                                    .searchAnalyzer("ik_smart")
                            )
                    )
                    .properties("summary", prop -> prop
                            .text(t -> t.analyzer("ik_smart"))
                    )
                    .properties("category", prop -> prop.keyword(k -> k))
                    .properties("keywords", prop -> prop.keyword(k -> k))
                    .properties("sizeInBytes", prop -> prop.long_(l -> l))
                    .properties("lastModified", prop -> prop.date(d -> d))
            );

            log.info("创建ES索引[{}]成功", FILE_ES_INDEX_NAME);
        } catch (IOException ex) {
            log.error("创建ES索引[{}]失败: {}", FILE_ES_INDEX_NAME, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private IndexResponse index(IndexRequest<EsFile> req) {
        try {
            return esClient.index(req);
        } catch (IOException ex) {
            log.error("ES索引[{}]失败: {}", req.document().getId(), ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
