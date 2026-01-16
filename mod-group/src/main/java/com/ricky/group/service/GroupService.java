package com.ricky.group.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.group.command.AddGroupManagersCommand;
import com.ricky.group.command.AddGroupMembersCommand;
import com.ricky.group.command.CreateGroupCommand;
import com.ricky.group.command.RenameGroupCommand;

public interface GroupService {
    String createGroup(CreateGroupCommand command, UserContext userContext);

    void renameGroup(String groupId, RenameGroupCommand command, UserContext userContext);

    void addGroupMembers(String groupId, AddGroupMembersCommand command, UserContext userContext);

    void addGroupManagers(String groupId, AddGroupManagersCommand command, UserContext userContext);

    void removeGroupMember(String groupId, String memberId, UserContext userContext);

    void addGroupManager(String groupId, String memberId, UserContext userContext);

    void removeGroupManager(String groupId, String memberId, UserContext userContext);

    void deleteGroup(String groupId, UserContext userContext);

    void activateGroup(String groupId, UserContext userContext);

    void deactivateGroup(String groupId, UserContext userContext);
}
