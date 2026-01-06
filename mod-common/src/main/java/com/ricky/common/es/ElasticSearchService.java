package com.ricky.common.es;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;

import java.util.List;

/**
 * ES文件服务接口 - 精简为核心业务方法
 */
public interface ElasticSearchService<T> {

    /**
     * 索引单个对象
     */
    IndexResponse index(T obj);

    /**
     * 批量索引对象
     */
    BulkResponse indexBulk(List<T> objs);

    /**
     * 分页搜索
     */
    SearchResult<T> search(String keyword, int page, int size);

    /**
     * 根据ID删除对象
     */
    DeleteResponse deleteById(String id);

    /**
     * 批量删除对象
     */
    BulkResponse deleteBulk(List<String> ids);

    /**
     * 根据ID查询对象
     */
    T byId(String id);
}