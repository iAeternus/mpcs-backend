package com.ricky.common.domain.hierarchy;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class HierarchyNode extends AggregateRoot {

    /**
     * 层次结构ID（同一棵树的节点一致）
     */
    private String treeId;

    /**
     * 父节点ID（根节点为 null）
     */
    private String parentId;

    /**
     * 从根到当前节点的路径（Materialized Path）
     * 例如: A/B/C
     */
    private String path;

    protected HierarchyNode(String id, String treeId, String parentId, String parentPath, UserContext uc) {
        super(id, uc);
        this.treeId = treeId;
        this.parentId = parentId;

        if (parentId == null) {
            this.path = id;
        } else {
            requireNotBlank(parentPath, "parentPath must not be blank.");
            this.path = parentPath + NODE_ID_SEPARATOR + id;
        }
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public int level() {
        return path.split(NODE_ID_SEPARATOR).length;
    }

    public boolean isDescendantOf(HierarchyNode other) {
        return this.path.startsWith(other.path + NODE_ID_SEPARATOR);
    }

    public boolean isAncestorOf(HierarchyNode other) {
        return other.path.startsWith(this.path + NODE_ID_SEPARATOR);
    }

    /**
     * 移动节点（改变父节点）
     */
    public void changeParent(String newParentId, String newParentPath) {
        requireNotBlank(newParentPath, "newParentPath must not be blank.");
        this.parentId = newParentId;
        this.path = newParentPath + NODE_ID_SEPARATOR + getId();
    }

    /**
     * 子节点在级联更新路径时调用（受控修改）
     */
    public void resetPath(String newPath) {
        requireNotBlank(newPath, "newPath must not be blank.");
        this.path = newPath;
    }


    public boolean isInSameTree(HierarchyNode other) {
        return this.treeId.equals(other.treeId);
    }
}
