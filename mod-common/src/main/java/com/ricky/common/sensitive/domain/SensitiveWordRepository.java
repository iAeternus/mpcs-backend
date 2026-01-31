package com.ricky.common.sensitive.domain;

import java.util.List;

public interface SensitiveWordRepository {

    void insert(List<SensitiveWord> sensitiveWords);

    List<SensitiveWord> findAll();

}
