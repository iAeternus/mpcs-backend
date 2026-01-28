package com.ricky.common.domain.hierarchy;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

public interface Hierarchy {

    /**
     * 根据ID获取从根节点到该节点的完整路径
     *
     * @param id 节点ID
     * @return 从根节点到该节点的完整路径
     */
    String schemaOf(String id);

    /**
     * 计算ID对应的节点的高度
     *
     * @param id 节点ID
     * @return 树高
     */
    default int levelOf(String id) {
        requireNotBlank(id, "Node ID must not be null.");

        return StringUtils.countMatches(schemaOf(id), NODE_ID_SEPARATOR) + 1;
    }

    /**
     * 获取树中所有ID
     *
     * @return 节点ID集合
     */
    Set<String> allIds();

    /**
     * 判断节点ID对应的节点是否存在
     *
     * @param id 节点ID
     * @return true=存在 false=不存在
     */
    default boolean containsId(String id) {
        return allIds().contains(id);
    }

    /**
     * 获取给定父节点ID之下一层的所有子节点ID
     *
     * @param parentId 父节点ID
     * @return 子节点ID集合
     */
    Set<String> directChildIdsUnder(String parentId);

    /**
     * 获取根节点下一层所有子节点ID
     *
     * @return 子节点ID集合
     */
    Set<String> allRootIds();

    /**
     * 获取给定节点的所有兄弟节点ID
     *
     * @param id 节点ID
     * @return 兄弟节点ID集合
     */
    Set<String> siblingIdsOf(String id);

    /**
     * 判断给定节点ID所对应节点是否为根节点
     *
     * @param id 节点ID
     * @return true=是 false=否
     */
    default boolean isRoot(String id) {
        requireNotBlank(id, "Node ID must not be null.");

        return !schemaOf(id).contains(NODE_ID_SEPARATOR);
    }

    /**
     * 获取给定节点ID对应节点的所有子节点ID，包括该节点
     *
     * @param id 节点ID
     * @return 子节点ID集合
     */
    Set<String> withAllChildIdsOf(String id);

    /**
     * 获取给定节点ID对应节点的所有子节点ID，不包括该节点
     *
     * @param id 节点ID
     * @return 子节点ID集合
     */
    Set<String> allChildIdsOf(String id);

    /**
     * 获取给定节点ID对应节点的所有父节点ID，包括该节点
     *
     * @param id 节点ID
     * @return 父节点ID集合
     */
    default Set<String> withAllParentIdsOf(String id) {
        requireNotBlank(id, "Node ID must not be null.");

        return Set.of(schemaOf(id).split(NODE_ID_SEPARATOR));
    }

    /**
     * 获取给定节点ID对应节点的所有父节点ID，不包括该节点
     *
     * @param id 节点ID
     * @return 父节点ID集合
     */
    default Set<String> allParentIdsOf(String id) {
        requireNotBlank(id, "Node ID must not be null.");

        return Arrays.stream(schemaOf(id).split(NODE_ID_SEPARATOR))
                .filter(aId -> !Objects.equals(aId, id))
                .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * 获取所有节点全名，并组织成映射<br>
     * 将映射集合中的值（从根节点到该节点的完整路径）中的每个节点ID转换为节点名
     *
     * @param allNames 节点ID-节点名 映射
     * @return 节点全名映射，节点ID-从根节点到该节点的完整路径，由节点名构成
     */
    Map<String, String> fullNames(Map<String, String> allNames);

}
