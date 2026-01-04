package com.ricky.common.es;

import java.util.List;

public interface ElasticSearchService<T> {

    /**
     * 上传对象至ES
     */
    void upload(T obj);

    /**
     * 批量上传对象至ES
     */
    void uploadBatch(List<T> objs);

    /**
     * 根据关键词搜索
     */
    default List<T> search(String keyword) {
        return pageSearch(keyword, 0, 20, true);
    }

    /**
     * 分页搜索
     *
     * @param keyword   搜索关键词
     * @param page      页码
     * @param size      每页大小
     * @param highlight 是否高亮显示
     * @return 搜索结果
     */
    List<T> pageSearch(String keyword, int page, int size, boolean highlight);

    /**
     * 根据ID删除对象
     */
    void removeById(String id);

}
