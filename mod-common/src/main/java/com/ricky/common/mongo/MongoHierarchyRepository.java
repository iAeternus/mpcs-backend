package com.ricky.common.mongo;

import com.ricky.common.domain.hierarchy.HierarchyNode;
import com.ricky.common.domain.hierarchy.HierarchyRepository;
import com.ricky.common.exception.MyException;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.exception.ErrorCodeEnum.AR_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@SuppressWarnings({"unchecked"})
public class MongoHierarchyRepository<T extends HierarchyNode>
        extends MongoBaseRepository<T> implements HierarchyRepository<T> {

    @Override
    public void save(T node) {
        super.save(node);
    }

    @Override
    public void save(List<T> nodes) {
        super.save(nodes);
    }

    @Override
    public T byId(String treeId, String id) {
        Query query = query(where("treeId").is(treeId).and("_id").is(id));
        T node = (T) mongoTemplate.findOne(query, arClass());
        if (node == null) {
            throw new MyException(AR_NOT_FOUND, "未找到节点。",
                    "type", arClass().getSimpleName(),
                    "treeId", treeId,
                    "id", id);
        }
        return node;
    }

    @Override
    public List<T> findAllDescendants(String treeId, String path) {
        Query query = query(
                where("treeId").is(treeId)
                        .and("path").regex("^" + path + NODE_ID_SEPARATOR)
        );
        return mongoTemplate.find(query, arClass());
    }

    @Override
    public List<T> findSubtree(String treeId, String path) {
        Query query = query(
                where("treeId").is(treeId)
                        .and("path").regex("^" + path)
        );
        return mongoTemplate.find(query, arClass());
    }

    @Override
    public List<T> findDirectChildren(String treeId, String parentId) {
        Query query = query(
                where("treeId").is(treeId)
                        .and("parentId").is(parentId)
        );
        return mongoTemplate.find(query, arClass());
    }

    @Override
    @Transactional
    public void deleteSubtree(String treeId, String path) {
        List<T> subtree = findSubtree(treeId, path);
        if (isEmpty(subtree)) {
            return;
        }
        super.delete(subtree);
    }

    @Override
    @Transactional
    public void moveNode(String treeId, String nodeId, String newParentId) {
        T node = byId(treeId, nodeId);
        T newParent = byId(treeId, newParentId);

        if (!node.isInSameTree(newParent)) {
            throw new IllegalStateException("Cannot move node across different trees.");
        }

        String oldPath = node.getPath();
        String newParentPath = newParent.getPath();

        if (newParentPath.startsWith(oldPath)) {
            throw new IllegalStateException("Cannot move node under its own subtree.");
        }

        String newPath = newParentPath + NODE_ID_SEPARATOR + node.getId();

        List<T> subtree = findSubtree(treeId, oldPath);

        // 根节点换父
        node.changeParent(newParentId, newParentPath);

        // 子节点级联更新 path
        for (T child : subtree) {
            if (child.getId().equals(nodeId)) continue;
            child.resetPath(child.getPath().replaceFirst(oldPath, newPath));
        }

        super.save(subtree);
    }
}
