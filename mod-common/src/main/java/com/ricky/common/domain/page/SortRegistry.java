package com.ricky.common.domain.page;

import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

import static com.ricky.common.utils.ValidationUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

public class SortRegistry {

    private final Map<String, String> fieldMap = new HashMap<>();

    private SortRegistry() {}

    public static SortRegistry newInstance() {
        return new SortRegistry();
    }

    public SortRegistry register(String apiField, String dbField) {
        fieldMap.put(apiField, dbField);
        return this;
    }

    public Sort resolve(String sortedBy, boolean asc) {
        if (isBlank(sortedBy) || !fieldMap.containsKey(sortedBy)) {
            return Sort.by(DESC, "createdAt");
        }
        return Sort.by(asc ? ASC : DESC, fieldMap.get(sortedBy));
    }
}
