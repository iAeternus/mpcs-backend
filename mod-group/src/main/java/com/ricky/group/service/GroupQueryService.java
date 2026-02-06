package com.ricky.group.service;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.group.query.*;

public interface GroupQueryService {
    GroupFoldersResponse fetchGroupFolders(String groupId);

    GroupOrdinaryMembersResponse fetchGroupOrdinaryMembers(String groupId, UserContext userContext);

    GroupManagersResponse fetchGroupManagers(String groupId, UserContext userContext);

    PagedList<GroupResponse> pageMyGroupsAsForManager(MyGroupsAsForManagerPageQuery pageQuery, UserContext userContext);

    PagedList<GroupResponse> pageMyGroupsAsForMember(MyGroupsAsForMemberPageQuery query, UserContext userContext);
}
