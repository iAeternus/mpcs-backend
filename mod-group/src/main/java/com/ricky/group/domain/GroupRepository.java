package com.ricky.group.domain;

import com.ricky.common.domain.user.UserContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GroupRepository {
    List<Group> byIds(Set<String> groupIds);

    boolean cachedExistsByName(String name, String userId);

    void save(Group group);

    void delete(Group group);

    List<UserCachedGroup> cachedUserAllGroups(String userId);

    Group byId(String groupId);

    Optional<Group> byIdOptional(String groupId);
}
