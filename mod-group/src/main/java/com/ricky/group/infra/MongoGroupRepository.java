package com.ricky.group.infra;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.GroupRepository;
import com.ricky.group.domain.UserCachedGroup;
import com.ricky.group.domain.UserCachedGroups;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

@Repository
@RequiredArgsConstructor
public class MongoGroupRepository extends MongoBaseRepository<Group> implements GroupRepository {

    private final MongoCachedGroupRepository cachedGroupRepository;

    @Override
    public List<Group> byIds(Set<String> groupIds) {
        return super.byIds(groupIds);
    }

    @Override
    public boolean cachedExistsByName(String name, String userId) {
        requireNotBlank(userId, "User ID must not be blank.");

        return cachedGroupRepository.cachedUserAllGroups(userId).getGroups().stream()
                .anyMatch(g -> ValidationUtils.equals(g.getName(), name));
    }

    @Override
    public void save(Group group) {
        super.save(group);
        cachedGroupRepository.evictUserGroupsCache(group.getUserId());
    }

    @Override
    public void delete(Group group) {
        super.delete(group);
        cachedGroupRepository.evictUserGroupsCache(group.getUserId());
    }

    @Override
    public List<UserCachedGroup> cachedUserAllGroups(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");

        return cachedGroupRepository.cachedUserAllGroups(userId).getGroups();
    }

    @Override
    public Group byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Group> byIdOptional(String id) {
        return super.byIdOptional(id);
    }
}
