package com.ricky.common.domain.hierarchy;

import java.util.List;

public interface HierarchyRepository<T extends HierarchyNode> {

    /**
     * 保存
     */
    void save(T node);

    /**
     * 批量保存
     */
    void save(List<T> nodes);

    /**
     * 查询树下的节点
     */
    T byId(String customId, String nodeId);

    /**
     * 删除节点
     */
    void delete(T node);

    /**
     * 批量删除
     */
    void delete(List<T> subtree);

    /**
     * 查询某节点下所有子孙节点（不含自身）
     */
    List<T> findAllDescendants(String customId, String path);

    /**
     * 查询某节点及其子树（含自身）
     */
    List<T> findSubtree(String customId, String path);

    /**
     * 查询直接子节点
     */
    List<T> findDirectChildren(String customId, String parentId);

}
