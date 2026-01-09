package com.ricky.group.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.group.command.CreateGroupCommand;
import com.ricky.group.domain.GroupRepository;
import com.ricky.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final RateLimiter rateLimiter;
    private final GroupRepository groupRepository;

    @Override
    public String createGroup(CreateGroupCommand command, UserContext userContext) {
        rateLimiter.applyFor("Group:CreateGroup", 10);



        return "";
    }
}
