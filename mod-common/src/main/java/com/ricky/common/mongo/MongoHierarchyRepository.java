package com.ricky.common.mongo;

import com.ricky.common.domain.hierarchy.HierarchyNode;
import com.ricky.common.domain.hierarchy.HierarchyRepository;
import com.ricky.common.exception.MyException;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.exception.ErrorCodeEnum.AR_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@SuppressWarnings({"unchecked"})
public abstract class MongoHierarchyRepository<T extends HierarchyNode>
        extends MongoBaseRepository<T> implements HierarchyRepository<T> {

    @Override
    public T byId(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        Query query = query(where("customId").is(customId).and("_id").is(nodeId));
        T node = (T) mongoTemplate.findOne(query, arClass());
        if (node == null) {
            throw new MyException(AR_NOT_FOUND, "未找到节点。",
                    "type", arClass().getSimpleName(),
                    "customId", customId,
                    "id", nodeId);
        }
        return node;
    }

    @Override
    public Optional<T> byIdOptional(String customId, String nodeId) {
        requireNotBlank(customId, "Custom Id cannot be blank");
        requireNotBlank(nodeId, "Node ID must not be blank");

        Query query = query(where("customId").is(customId).and("_id").is(nodeId));
        T node = (T) mongoTemplate.findOne(query, arClass());
        return Optional.ofNullable(node);
    }

    @Override
    public List<T> byIds(String customId, Set<String> ids) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        Query query = query(where("customId").is(customId).and("_id").in(ids));
        List<T> nodes = mongoTemplate.find(query, arClass());
        return List.copyOf(nodes);
    }

    @Override
    public List<T> findAllDescendants(String customId, String path) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        Query query = query(
                where("customId").is(customId)
                        .and("path").regex("^" + path + NODE_ID_SEPARATOR)
        );
        return mongoTemplate.find(query, arClass());
    }

    @Override
    public List<T> findSubtree(String customId, String path) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        Query query = query(
                where("customId").is(customId)
                        .and("path").regex("^" + path)
        );
        return mongoTemplate.find(query, arClass());
    }

    @Override
    public List<T> findDirectChildren(String customId, String parentId) {
        requireNotBlank(customId, "Custom Id cannot be blank");

        Query query = query(
                where("customId").is(customId)
                        .and("parentId").is(parentId)
        );
        return mongoTemplate.find(query, arClass());
    }
}
