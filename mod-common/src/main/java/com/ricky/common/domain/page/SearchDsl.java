package com.ricky.common.domain.page;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.Map;

import static com.ricky.common.validation.id.IdValidator.isId;

public class SearchDsl {

    public static Criteria byIdOr(Criteria base,
                                  String search,
                                  String idPrefix,
                                  String idField,
                                  String... textFields) {
        if (isId(search, idPrefix)) {
            return base.and(idField).is(search);
        }

        Criteria[] textCriteria = Arrays.stream(textFields)
                .map(f -> Criteria.where(f).regex(search))
                .toArray(Criteria[]::new);

        return base.orOperator(textCriteria);
    }

    public static Criteria byMultiId(Criteria base,
                                     String search,
                                     Map<String, String> prefixToField) {
        for (var e : prefixToField.entrySet()) {
            if (isId(search, e.getKey())) {
                return base.and(e.getValue()).is(search);
            }
        }
        return base;
    }
}
