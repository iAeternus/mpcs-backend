package com.ricky.group.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.group.command.CreateGroupCommand;

public interface GroupService {
    String createGroup(CreateGroupCommand command, UserContext userContext);
}
