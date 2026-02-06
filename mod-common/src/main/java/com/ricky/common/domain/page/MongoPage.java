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

public class MongoPage<AR extends AggregateRoot, Q extends PageQuery> {

    private final Class<AR> arClass;
    private final String collectionName;

    private Q query;
    private Criteria criteria = new Criteria();
    private Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    private final List<String> includeFields = new ArrayList<>();

    private MongoPage(Class<AR> arClass, String collectionName) {
        this.arClass = arClass;
        this.collectionName = collectionName;
    }

    public static <AR extends AggregateRoot> MongoPage<AR, ?> of(Class<AR> arClass, String collectionName) {
        return new MongoPage<>(arClass, collectionName);
    }

    @SuppressWarnings("unchecked")
    public <T extends PageQuery> MongoPage<AR, T> pageQuery(T query) {
        this.query = (Q) query;
        return (MongoPage<AR, T>) this;
    }

    public MongoPage<AR, Q> where(Function<Criteria, Criteria> criteriaBuilder) {
        this.criteria = criteriaBuilder.apply(new Criteria());
        return this;
    }

    public MongoPage<AR, Q> and(Function<Criteria, Criteria> criteriaBuilder) {
        this.criteria = criteriaBuilder.apply(this.criteria);
        return this;
    }

    public MongoPage<AR, Q> noSpecifiedField() {
        this.criteria = new Criteria();
        return this;
    }

    public MongoPage<AR, Q> search(SearchCriteriaBuilder<Q> builder) {
        if (query instanceof SearchablePageQuery spq) {
            String search = spq.getSearch();
            if (isNotBlank(search)) {
                this.criteria = builder.build(search, this.criteria, query);
            }
        }
        return this;
    }

    public MongoPage<AR, Q> sort(SortRegistry sortRegistry) {
        if (query instanceof SearchablePageQuery spq) {
            this.sort = sortRegistry.resolve(spq.getSortedBy(), spq.asc());
        }
        return this;
    }

    public MongoPage<AR, Q> sort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public MongoPage<AR, Q> sort(Function<Q, Sort> sortBuilder) {
        if (nonNull(query)) {
            this.sort = sortBuilder.apply(query);
        }
        return this;
    }

    public MongoPage<AR, Q> project(String... fields) {
        this.includeFields.addAll(List.of(fields));
        return this;
    }

    public <R> PagedList<R> map(Function<List<AR>, List<R>> mapper, MongoTemplate mongoTemplate) {
        return execute(mongoTemplate, query -> {
                    List<AR> ars = mongoTemplate.find(query, arClass);
                    return mapper.apply(ars);
                }
        );
    }

    public <R> PagedList<R> fetchAs(Class<R> resultType, MongoTemplate mongoTemplate) {
        return execute(mongoTemplate,
                query -> mongoTemplate.find(query, resultType, collectionName));
    }

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
