package com.ricky.group.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MongoGroupRepository extends MongoBaseRepository<Group> implements GroupRepository {
    @Override
    public List<Group> byIds(Set<String> groupIds) {
        return super.byIds(groupIds);
    }
}
