package com.ricky.group.service;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.group.query.GroupFoldersResponse;
import com.ricky.group.query.GroupMembersResponse;
import com.ricky.group.query.GroupFoldersPageQuery;

public interface GroupQueryService {
    GroupMembersResponse listGroupMembers(String groupId, UserContext userContext);

    PagedList<GroupFoldersResponse> pageGroupFolders(String groupId, GroupFoldersPageQuery query, UserContext userContext);
}
