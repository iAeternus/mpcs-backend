package com.ricky.group.domain;

import com.ricky.common.domain.marker.ValueObject;
import com.ricky.common.permission.Permission;
import com.ricky.common.utils.ValidationUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCachedGroup implements ValueObject {

    String id;
    String name;
    boolean active;
    String userId;
    List<Member> members;
    Map<String, Set<Permission>> grants;
    InheritancePolicy inheritancePolicy;

    public boolean isVisible() {
        return active;
    }

    public boolean containsManager(String managerId) {
        if (ValidationUtils.isEmpty(members)) {
            return false;
        }
        return members.stream()
                .anyMatch(member -> ValidationUtils.equals(member.getUserId(), managerId)
                        && member.getRole() == MemberRole.ADMIN);
    }

    public boolean containsMember(String memberId) {
        if (ValidationUtils.isEmpty(members)) {
            return false;
        }
        return members.stream()
                .anyMatch(member -> ValidationUtils.equals(member.getUserId(), memberId));
    }
}
