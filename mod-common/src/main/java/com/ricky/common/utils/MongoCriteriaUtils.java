package com.ricky.common.utils;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;

import static com.ricky.common.utils.CommonUtils.splitSearchBySpace;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoCriteriaUtils {

    public static Criteria regexSearch(String field, String search) {
        Criteria criteria = new Criteria();
        if (isBlank(search)) {
            return criteria;
        }

        return criteria.orOperator(Arrays.stream(splitSearchBySpace(search))
                .map(s -> where(field).regex(s))
                .toArray(Criteria[]::new));
    }

}
