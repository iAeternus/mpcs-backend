package com.ricky.group.domain;

import java.util.List;
import java.util.Set;

public interface GroupRepository {
    List<Group> byIds(Set<String> groupIds);
}
