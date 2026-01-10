package com.ricky.group.service.impl;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.group.query.GroupFoldersResponse;
import com.ricky.group.query.GroupMembersResponse;
import com.ricky.group.query.PageGroupFoldersQuery;
import com.ricky.group.service.GroupQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupQueryServiceImpl implements GroupQueryService {
    @Override
    public GroupMembersResponse listGroupMembers(String groupId, UserContext userContext) {
        return null;
    }

    @Override
    public PagedList<GroupFoldersResponse> pageGroupFolders(String groupId, PageGroupFoldersQuery query, UserContext userContext) {
        return null;
    }
}
