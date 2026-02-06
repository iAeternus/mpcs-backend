package com.ricky.common.domain.hierarchy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * 查询树下的节点，返回Optional
     */
    Optional<T> byIdOptional(String customId, String nodeId);

    /**
     * 批量查询节点
     */
    List<T> byIds(String customId, Set<String> ids);

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
     * 查询某节点及其所有子孙节点（含自身）
     */
    List<T> findSubtree(String customId, String path);

    /**
     * 查询直接子节点
     */
    List<T> findDirectChildren(String customId, String parentId);
}
