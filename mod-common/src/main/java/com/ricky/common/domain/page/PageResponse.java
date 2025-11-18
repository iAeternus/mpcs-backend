package com.ricky.common.domain.page;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/3/12
 * @className PageVO
 * @desc 分页查询响应体
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResponse<T> implements Response {

    /**
     * 总数量
     */
    Integer totalCnt;

    /**
     * 页号
     */
    Integer pageIndex;

    /**
     * 每页条数
     */
    Integer pageSize;

    /**
     * 数据
     */
    List<T> data;

}