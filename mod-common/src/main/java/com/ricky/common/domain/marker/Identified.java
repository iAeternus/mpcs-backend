package com.ricky.common.domain.marker;


import java.io.Serializable;
import java.util.Collection;

import static com.ricky.common.utils.ValidationUtil.isEmpty;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/5
 * @className Identified
 * @desc 标识符marker接口
 */
public interface Identified extends Serializable {

    /**
     * 判断id是否重复
     *
     * @param collection id集合
     * @return true=id在集合中重复 false=id在集合中不重复
     */
    static boolean isDuplicated(Collection<? extends Identified> collection) {
        if (isEmpty(collection)) {
            return false;
        }

        long count = collection.stream()
                .map(Identified::getId)
                .distinct()
                .count();
        return count != collection.size();
    }

    /**
     * 获取标识符
     *
     * @return 标识符
     */
    String getId();

}
