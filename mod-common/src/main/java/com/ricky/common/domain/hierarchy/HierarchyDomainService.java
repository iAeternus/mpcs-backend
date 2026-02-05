package com.ricky.common.domain.hierarchy;

import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.exception.ErrorCodeEnum.HIERARCHY_ERROR;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static java.util.stream.Collectors.joining;

@Service
@RequiredArgsConstructor
public class HierarchyDomainService<T extends HierarchyNode> {

    private final HierarchyRepository<T> repository;

    /**
     * 根据ID获取从根节点到该节点的完整路径
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 从根节点到该节点的完整路径
     */
    public String schemaOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");
        return repository.byId(customId, nodeId).getPath();
    }

    /**
     * 计算ID对应的节点的高度
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 树高
     */
    public int levelOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        return StringUtils.countMatches(schemaOf(customId, nodeId), NODE_ID_SEPARATOR) + 1;
    }

    /**
     * 获取树中所有ID
     *
     * @param customId 树ID
     * @return 节点ID集合
     */
    public Set<String> allIds(String customId) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        return repository.findSubtree(customId, "")
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    /**
     * 判断节点ID对应的节点是否存在
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return true=存在 false=不存在
     */
    public boolean containsId(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        return allIds(customId).contains(nodeId);
    }

    /**
     * 获取给定父节点ID之下一层的所有子节点ID
     *
     * @param customId 树ID
     * @param parentId 父节点ID
     * @return 子节点ID集合
     */
    public Set<String> directChildIdsUnder(String customId, String parentId) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        return repository.findDirectChildren(customId, parentId)
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    /**
     * 获取根节点下一层所有子节点ID
     *
     * @param customId 树ID
     * @return 子节点ID集合
     */
    public Set<String> allRootIds(String customId) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        return repository.findDirectChildren(customId, null)
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    /**
     * 获取给定节点的所有兄弟节点ID
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 兄弟节点ID集合
     */
    public Set<String> siblingIdsOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        T node = repository.byId(customId, nodeId);
        return repository.findDirectChildren(customId, node.getParentId())
                .stream()
                .map(HierarchyNode::getId)
                .filter(i -> !i.equals(nodeId))
                .collect(toImmutableSet());
    }

    /**
     * 判断给定节点ID所对应节点是否为根节点
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return true=是 false=否
     */
    public boolean isRoot(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        return !schemaOf(customId, nodeId).contains(NODE_ID_SEPARATOR);
    }

    /**
     * 获取给定节点ID对应节点的父节点ID
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 父节点ID，若该节点没有父节点则返回null
     */
    private String parentIdOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        String[] ids = schemaOf(customId, nodeId).split(NODE_ID_SEPARATOR);
        if (ids.length <= 1) {
            return null;
        }
        return ids[ids.length - 2];
    }

    /**
     * 获取给定节点ID对应节点的所有子节点ID，包括该节点
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 子节点ID集合
     */
    public Set<String> withAllChildIdsOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        T node = repository.byId(customId, nodeId);
        return repository.findSubtree(customId, node.getPath())
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    /**
     * 获取给定节点ID对应节点的所有子节点ID，不包括该节点
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 子节点ID集合
     */
    public Set<String> allChildIdsOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        T node = repository.byId(customId, nodeId);
        return repository.findAllDescendants(customId, node.getPath())
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    /**
     * 获取给定节点ID对应节点的所有父节点ID，包括该节点
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 父节点ID集合
     */
    public Set<String> withAllParentIdsOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        return Set.of(schemaOf(customId, nodeId).split(NODE_ID_SEPARATOR));
    }

    /**
     * 获取给定节点ID对应节点的所有父节点ID，不包括该节点
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 父节点ID集合
     */
    public Set<String> allParentIdsOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        return Arrays.stream(schemaOf(customId, nodeId).split(NODE_ID_SEPARATOR))
                .filter(aId -> !Objects.equals(aId, nodeId))
                .collect(toImmutableSet());
    }

    /**
     * 获取所有节点全名，并组织成映射<br>
     * 将映射集合中的值（从根节点到该节点的完整路径）中的每个节点ID转换为节点名
     *
     * @param customId 树ID
     * @param allNames 节点ID-节点名 映射
     * @return 节点全名映射，节点ID-从根节点到该节点的完整路径，由节点名构成
     */
    public Map<String, String> fullNames(String customId, Map<String, String> allNames) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        return allNames.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey,
                        e -> buildFullName(customId, e.getKey(), allNames)));
    }

    private String buildFullName(String customId, String nodeId, Map<String, String> names) {
        T node = repository.byId(customId, nodeId);
        String[] parts = node.getPath().split(NODE_ID_SEPARATOR);

        return Arrays.stream(parts)
                .map(pid -> names.getOrDefault(pid, ""))
                .filter(s -> !s.isBlank())
                .collect(joining(NODE_ID_SEPARATOR));
    }

    /**
     * 删除整棵子树
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     */
    public void deleteSubtree(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        T node = repository.byId(customId, nodeId);
        List<T> subtree = repository.findSubtree(customId, node.getPath());
        if (isEmpty(subtree)) {
            return;
        }
        repository.delete(subtree);
    }

    /**
     * 移动节点以及整棵子树
     *
     * @param customId    树ID
     * @param nodeId      节点ID
     * @param newParentId 新父节点ID
     */
    public void moveNode(String customId, String nodeId, String newParentId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");
        requireNotBlank(newParentId, "Parent ID must not be blank");

        T node = repository.byId(customId, nodeId);
        T newParent = repository.byId(customId, newParentId);

        if (!node.isInSameTree(newParent)) {
            throw new MyException(HIERARCHY_ERROR, "Cannot merge nodes from different trees",
                    "nodeId", nodeId, "newParentId", newParentId);
        }

        String oldPath = node.getPath();
        String newParentPath = newParent.getPath();

        if (newParentPath.startsWith(oldPath)) {
            throw new MyException(HIERARCHY_ERROR, "Cannot move node under its own subtree",
                    "nodeId", nodeId, "newParentId", newParentId);
        }

        String newPath = newParentPath + NODE_ID_SEPARATOR + node.getId();

        List<T> subtree = repository.findSubtree(customId, oldPath);

        // 根节点换父
        node.changeParent(newParentId, newParentPath);

        // 子节点级联更新 path
        for (T child : subtree) {
            if (child.getId().equals(nodeId)) continue;
            child.resetPath(child.getPath().replaceFirst(oldPath, newPath));
        }

        repository.save(subtree);
    }

    /**
     * 合并两棵树
     *
     * @param customId     树ID
     * @param targetNodeId 目标子树的根节点ID
     * @param sourceNodeId 源子树的根节点ID
     * @note 源子树作为一棵子树整体，移动到目标子树下
     */
    public void merge(String customId, String targetNodeId, String sourceNodeId) {
        moveNode(customId, sourceNodeId, targetNodeId);
    }

}
