package com.ricky.common.domain.hierarchy;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.exception.ErrorCodeEnum.HIERARCHY_ERROR;
import static com.ricky.common.utils.ValidationUtils.*;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public abstract class HierarchyDomainService<T extends HierarchyNode, R extends HierarchyRepository<T>> {

    protected final R repository;

    /**
     * 根据ID获取从根节点到该节点的完整路径
     *
     * @param customId 树ID
     * @param nodeId   节点ID
     * @return 从根节点到该节点的完整路径
     */
    public String schemaOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        if (isNull(nodeId)) {
            return "";
        }

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

        if (isNull(nodeId)) {
            return 0;
        }

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

        return repository.getSubtree(customId, "")
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

        if (isNull(nodeId)) {
            return false;
        }

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

        return repository.getDirectChildren(customId, parentId)
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

        return repository.getDirectChildren(customId, null)
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
        return repository.getDirectChildren(customId, node.getParentId())
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
        return isBlank(schemaOf(customId, nodeId));
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

        String path = repository.byIdOptional(customId, nodeId)
                .map(HierarchyNode::getPath)
                .orElse("");

        return repository.getSubtree(customId, path)
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

        String path = repository.byIdOptional(customId, nodeId)
                .map(HierarchyNode::getPath)
                .orElse("");

        return repository.getAllDescendants(customId, path)
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
    public List<String> withAllParentIdsOf(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        if (isNull(nodeId)) {
            return Collections.emptyList();
        }

        return Arrays.stream(schemaOf(customId, nodeId).split(NODE_ID_SEPARATOR))
                .collect(toImmutableList());
    }

    /**
     * 获取给定节点ID对应节点的所有父节点ID，包括该节点，逆序排列<br>
     * 顺序：[self, parent, parent.parent, ..., root]
     */
    public List<String> withAllParentIdsRev(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        if (isNull(nodeId)) {
            return Collections.emptyList();
        }

        String[] nodeIds = schemaOf(customId, nodeId).split(NODE_ID_SEPARATOR);
        return IntStream.range(0, nodeIds.length)
                .map(i -> nodeIds.length - 1 - i)  // 逆序索引
                .mapToObj(i -> nodeIds[i])
                .collect(toImmutableList());
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

        if (isNull(nodeId)) {
            return Collections.emptySet();
        }

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

        String path = repository.byIdOptional(customId, nodeId)
                .map(HierarchyNode::getPath)
                .orElse("");

        List<T> subtree = repository.getSubtree(customId, path);
        if (isEmpty(subtree)) {
            return;
        }
        repository.delete(subtree);
    }

    /**
     * 移动节点以及整棵子树到目标节点下，不会落库
     *
     * @param customId 树ID
     * @param src      源节点
     * @param dst      目标节点
     * @return 整个子树，包括源节点
     */
    public List<T> moveNode(String customId, T src, T dst, UserContext userContext) {
        checkSameTree(src, dst);
        checkNotMoveIntoDescendant(src, dst);

        String oldPath = src.getPath();
        String newPath = dst.getPath() + NODE_ID_SEPARATOR + src.getId();

        List<T> subtree = repository.getSubtree(customId, oldPath);

        // 处理根节点
        T root = subtree.stream()
                .filter(node -> ValidationUtils.equals(node.getId(), src.getId()))
                .findFirst()
                .orElseThrow();
        root.moveTo(dst.getId(), dst.getPath(), userContext);

        // 处理后代节点
        subtree.stream()
                .filter(node -> notEquals(node.getId(), src.getId()))
                .forEach(child -> child.updatePath(oldPath, newPath, userContext));

        return subtree;
    }

    private void checkSameTree(T src, T dst) {
        if (notEquals(src.getCustomId(), dst.getCustomId())) {
            throw new MyException(HIERARCHY_ERROR, "不能移动到其他空间",
                    "srcCustomId", src.getCustomId(), "dstCustomId", dst.getCustomId());
        }
    }

    private void checkNotMoveIntoDescendant(T src, T dst) {
        if (dst.getPath().startsWith(src.getPath())) {
            throw new MyException(HIERARCHY_ERROR, "不能移动到子目录",
                    "srcPath", src.getPath(), "dstPath", dst.getPath());
        }
    }

    /**
     * 合并两棵树
     *
     * @param customId 树ID
     * @param src      源节点
     * @param dst      目标节点
     * @note 源子树作为一棵子树整体，移动到目标子树下
     */
    public void merge(String customId, T src, T dst, UserContext userContext) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNonNull(src, "Src Node must not be null");
        requireNonNull(dst, "Dst Node must not be null");

        moveNode(customId, src, dst, userContext);
    }

}
