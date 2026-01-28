package com.ricky.group.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ricky.common.exception.ErrorCodeEnum.GROUP_WITH_NAME_ALREADY_EXISTS;

@Component
@RequiredArgsConstructor
public class GroupFactory {

    private final GroupRepository groupRepository;

    public Group create(String name, String customId, UserContext userContext) {
        checkNameDuplication(name, userContext.getUid());
        return new Group(name, customId, userContext);
    }

    private void checkNameDuplication(String name, String userId) {
        if (groupRepository.cachedExistsByName(name, userId)) {
            throw new MyException(GROUP_WITH_NAME_ALREADY_EXISTS, "创建失败，名称已被占用。",
                    "name", name, "userId", userId);
        }
    }

}
