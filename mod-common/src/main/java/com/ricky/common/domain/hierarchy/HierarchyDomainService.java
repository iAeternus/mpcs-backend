package com.ricky.common.domain.hierarchy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static java.util.stream.Collectors.joining;

@Service
@RequiredArgsConstructor
public class HierarchyDomainService<T extends HierarchyNode> implements Hierarchy {

    private final HierarchyRepository<T> repository;
    private String treeId;

    protected void setTreeId(HierarchyNode node) {
        this.treeId = node.getTreeId();
    }

    @Override
    public String schemaOf(String id) {
        requireNotBlank(id, "Node ID must not be blank.");
        return repository.byId(treeId, id).getPath();
    }

    @Override
    public Set<String> allIds() {
        return repository.findSubtree(treeId, "")
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> directChildIdsUnder(String parentId) {
        return repository.findDirectChildren(treeId, parentId)
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> allRootIds() {
        return repository.findDirectChildren(treeId, null)
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> siblingIdsOf(String id) {
        T node = repository.byId(treeId, id);
        return repository.findDirectChildren(treeId, node.getParentId())
                .stream()
                .map(HierarchyNode::getId)
                .filter(i -> !i.equals(id))
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> withAllChildIdsOf(String id) {
        T node = repository.byId(treeId, id);
        return repository.findSubtree(treeId, node.getPath())
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    @Override
    public Set<String> allChildIdsOf(String id) {
        T node = repository.byId(treeId, id);
        return repository.findAllDescendants(treeId, node.getPath())
                .stream()
                .map(HierarchyNode::getId)
                .collect(toImmutableSet());
    }

    /**
     * 根据路径拼接全名：
     * A/B/C => nameA/nameB/nameC
     */
    @Override
    public Map<String, String> fullNames(Map<String, String> allNames) {
        return allNames.entrySet().stream()
                .collect(toImmutableMap(Map.Entry::getKey,
                        e -> buildFullName(e.getKey(), allNames)));
    }

    private String buildFullName(String id, Map<String, String> names) {
        T node = repository.byId(treeId, id);
        String[] parts = node.getPath().split(NODE_ID_SEPARATOR);

        return Arrays.stream(parts)
                .map(pid -> names.getOrDefault(pid, ""))
                .filter(s -> !s.isBlank())
                .collect(joining(NODE_ID_SEPARATOR));
    }
}
