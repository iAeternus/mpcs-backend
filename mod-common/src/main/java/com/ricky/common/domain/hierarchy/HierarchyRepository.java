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
    T byId(String treeId, String id);

    /**
     * 查询某节点下所有子孙节点（不含自己）
     */
    List<T> findAllDescendants(String treeId, String path);

    /**
     * 查询某节点及其子树（含自身）
     */
    List<T> findSubtree(String treeId, String path);

    /**
     * 查询直接子节点
     */
    List<T> findDirectChildren(String treeId, String parentId);

    /**
     * 删除整棵子树
     */
    void deleteSubtree(String treeId, String path);

    /**
     * 移动节点以及整棵子树
     */
    void moveNode(String treeId, String nodeId, String newParentId);

}
