package com.ricky.common.domain.page;

import com.ricky.common.domain.AggregateRoot;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.ricky.common.utils.ValidationUtils.isNotBlank;
import static com.ricky.common.utils.ValidationUtils.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * Mongo 分页查询 DSL
 * <p>
 * 用于以链式方式构建 MongoDB 分页查询条件（where / search / sort / project），
 * 并最终执行查询返回 {@link PagedList}
 *
 * <h3>Examples</h3>
 * <pre>{@code
 * return MongoPageQuery.of(Comment.class, "comment")
 *     .pageQuery(query)
 *     .where(c -> c.and("postId").is(query.getPostId()))
 *     .search(commentSearchBuilder)
 *     .sort(commentSortRegistry)
 *     .project("id", "content", "createdAt")
 *     .fetchAs(CommentResponse.class, mongoTemplate);
 * }</pre>
 *
 * @param <AR> 聚合根类型
 * @param <Q>  继承 {@link PageQuery} 的分页查询参数类型
 */
public class MongoPageQuery<AR extends AggregateRoot, Q extends PageQuery> {

    private final Class<AR> arClass;
    private final String collectionName;

    private Q query;
    private Criteria criteria = new Criteria();
    private Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    private final List<String> includeFields = new ArrayList<>();

    private MongoPageQuery(Class<AR> arClass, String collectionName) {
        this.arClass = arClass;
        this.collectionName = collectionName;
    }

    /**
     * 创建分页查询 DSL
     *
     * @param arClass        聚合根类型
     * @param collectionName Mongo 集合名
     */
    public static <AR extends AggregateRoot> MongoPageQuery<AR, ?> of(Class<AR> arClass, String collectionName) {
        return new MongoPageQuery<>(arClass, collectionName);
    }

    /**
     * 设置分页查询参数
     */
    @SuppressWarnings("unchecked")
    public <T extends PageQuery> MongoPageQuery<AR, T> pageQuery(T query) {
        this.query = (Q) query;
        return (MongoPageQuery<AR, T>) this;
    }

    /**
     * 设置查询条件（覆盖已有条件）
     */
    public MongoPageQuery<AR, Q> where(Function<Criteria, Criteria> criteriaBuilder) {
        this.criteria = criteriaBuilder.apply(new Criteria());
        return this;
    }

    /**
     * 追加查询条件
     */
    public MongoPageQuery<AR, Q> and(Function<Criteria, Criteria> criteriaBuilder) {
        this.criteria = criteriaBuilder.apply(this.criteria);
        return this;
    }

    /**
     * 清空所有已设置的查询条件
     */
    public MongoPageQuery<AR, Q> noSpecifiedField() {
        this.criteria = new Criteria();
        return this;
    }

    /**
     * 构建搜索条件（仅当 query 实现 {@link SearchablePageQuery} 时生效）
     */
    public MongoPageQuery<AR, Q> search(SearchCriteriaBuilder<Q> builder) {
        if (query instanceof SearchablePageQuery spq) {
            String search = spq.getSearch();
            if (isNotBlank(search)) {
                this.criteria = builder.build(search, this.criteria, query);
            }
        }
        return this;
    }

    /**
     * 根据排序注册表构建排序规则
     */
    public MongoPageQuery<AR, Q> sort(SortRegistry sortRegistry) {
        if (query instanceof SearchablePageQuery spq) {
            this.sort = sortRegistry.resolve(spq.getSortedBy(), spq.asc());
        }
        return this;
    }

    /**
     * 直接指定排序规则
     */
    public MongoPageQuery<AR, Q> sort(Sort sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 通过函数从查询参数构建排序规则
     */
    public MongoPageQuery<AR, Q> sort(Function<Q, Sort> sortBuilder) {
        if (nonNull(query)) {
            this.sort = sortBuilder.apply(query);
        }
        return this;
    }

    /**
     * 指定返回字段（Mongo projection）
     */
    public MongoPageQuery<AR, Q> project(String... fields) {
        this.includeFields.addAll(List.of(fields));
        return this;
    }

    /**
     * 查询聚合根并映射为结果对象，终结操作
     */
    public <R> PagedList<R> map(Function<List<AR>, List<R>> mapper, MongoTemplate mongoTemplate) {
        return execute(mongoTemplate, query -> {
                    List<AR> ars = mongoTemplate.find(query, arClass);
                    return mapper.apply(ars);
                }
        );
    }

    /**
     * 直接以指定类型查询结果，终结操作
     */
    public <R> PagedList<R> fetchAs(Class<R> resultType, MongoTemplate mongoTemplate) {
        return execute(mongoTemplate,
                query -> mongoTemplate.find(query, resultType, collectionName));
    }

    /**
     * 执行分页查询
     */
    private <R> PagedList<R> execute(MongoTemplate mongoTemplate, Function<Query, List<R>> fetcher) {
        requireNonNull(query, "pageQuery must not be null");

        Pagination pagination = Pagination.pagination(
                query.getPageIndex(),
                query.getPageSize()
        );

        Query mongoQuery = Query.query(criteria)
                .skip(pagination.skip())
                .limit(pagination.limit())
                .with(sort);

        if (!includeFields.isEmpty()) {
            includeFields.forEach(f -> mongoQuery.fields().include(f));
        }

        long count = mongoTemplate.count(Query.query(criteria), arClass);
        if (count == 0) {
            return PagedList.emptyList(pagination);
        }

        List<R> result = fetcher.apply(mongoQuery);
        return PagedList.pagedList(pagination, (int) count, result);
    }
}
