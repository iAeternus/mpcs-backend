package com.ricky.group;

import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.group.command.AddGrantCommand;
import com.ricky.group.command.AddGrantsCommand;
import com.ricky.group.command.AddGroupManagersCommand;
import com.ricky.group.command.AddGroupMembersCommand;
import com.ricky.group.command.CreateGroupCommand;
import com.ricky.group.command.RenameGroupCommand;
import com.ricky.group.query.FolderPermissionResponse;
import com.ricky.group.query.GroupFoldersResponse;
import com.ricky.group.query.GroupManagersResponse;
import com.ricky.group.query.GroupOrdinaryMembersResponse;
import com.ricky.group.query.GroupResponse;
import com.ricky.group.query.MyGroupsAsForManagerPageQuery;
import com.ricky.group.query.MyGroupsAsForMemberPageQuery;
import com.ricky.group.service.GroupQueryService;
import com.ricky.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.ricky.common.constants.ConfigConstants.GROUP_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.USER_ID_PREFIX;
import static com.ricky.common.domain.dto.resp.IdResponse.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@CrossOrigin
@RestController
@Tag(name = "group")
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final GroupQueryService groupQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "create group")
    public IdResponse createGroup(@RequestBody @Valid CreateGroupCommand command,
                                  @AuthenticationPrincipal UserContext userContext) {
        return returnId(groupService.createGroup(command, userContext));
    }

    @PutMapping("/{groupId}/name")
    @Operation(summary = "rename group")
    public void renameGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                            @RequestBody @Valid RenameGroupCommand command,
                            @AuthenticationPrincipal UserContext userContext) {
        groupService.renameGroup(groupId, command, userContext);
    }

    @PutMapping("/{groupId}/members")
    @Operation(summary = "add group members")
    public void addGroupMembers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                @RequestBody @Valid AddGroupMembersCommand command,
                                @AuthenticationPrincipal UserContext userContext) {
        groupService.addGroupMembers(groupId, command, userContext);
    }

    @PutMapping("/{groupId}/managers")
    @Operation(summary = "add group managers")
    public void addGroupManagers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                 @RequestBody @Valid AddGroupManagersCommand command,
                                 @AuthenticationPrincipal UserContext userContext) {
        groupService.addGroupManagers(groupId, command, userContext);
    }

    @PutMapping("/{groupId}/managers/{memberId}")
    @Operation(summary = "add group manager")
    public void addGroupManager(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                @PathVariable @NotBlank @Id(USER_ID_PREFIX) String memberId,
                                @AuthenticationPrincipal UserContext userContext) {
        groupService.addGroupManager(groupId, memberId, userContext);
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @Operation(summary = "remove group member")
    public void removeGroupMember(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                  @PathVariable @NotBlank @Id(USER_ID_PREFIX) String memberId,
                                  @AuthenticationPrincipal UserContext userContext) {
        groupService.removeGroupMember(groupId, memberId, userContext);
    }

    @DeleteMapping("/{groupId}/managers/{memberId}")
    @Operation(summary = "remove group manager")
    public void removeGroupManager(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                   @PathVariable @NotBlank @Id(USER_ID_PREFIX) String memberId,
                                   @AuthenticationPrincipal UserContext userContext) {
        groupService.removeGroupManager(groupId, memberId, userContext);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "delete group")
    public void deleteGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                            @AuthenticationPrincipal UserContext userContext) {
        groupService.deleteGroup(groupId, userContext);
    }

    @PutMapping("/{groupId}/activation")
    @Operation(summary = "activate group")
    public void activateGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                              @AuthenticationPrincipal UserContext userContext) {
        groupService.activateGroup(groupId, userContext);
    }

    @PutMapping("/{groupId}/deactivation")
    @Operation(summary = "deactivate group")
    public void deactivateGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                @AuthenticationPrincipal UserContext userContext) {
        groupService.deactivateGroup(groupId, userContext);
    }

    @PutMapping("/grant")
    @Operation(summary = "grant member folder permission")
    public void addGrant(@RequestBody @Valid AddGrantCommand command,
                         @AuthenticationPrincipal UserContext userContext) {
        groupService.addGrant(command, userContext);
    }

    @PutMapping("/grants")
    @Operation(summary = "grant member folder permissions")
    public void addGrants(@RequestBody @Valid AddGrantsCommand command,
                          @AuthenticationPrincipal UserContext userContext) {
        groupService.addGrants(command, userContext);
    }

    @Deprecated
    @GetMapping("/{groupId}/folders")
    @Operation(summary = "fetch granted folders")
    public GroupFoldersResponse fetchGroupFolders(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                                  @RequestParam(required = false) @Id(USER_ID_PREFIX) String memberId,
                                                  @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.fetchGroupFolders(groupId, memberId, userContext);
    }

    @GetMapping("/{groupId}/ordinary-member")
    @Operation(summary = "fetch ordinary members")
    public GroupOrdinaryMembersResponse fetchGroupOrdinaryMembers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                                                  @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.fetchGroupOrdinaryMembers(groupId, userContext);
    }

    @GetMapping("/{groupId}/managers")
    @Operation(summary = "fetch managers")
    public GroupManagersResponse fetchGroupManagers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                                    @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.fetchGroupManagers(groupId, userContext);
    }

    @PostMapping("/page/my-groups")
    @Operation(summary = "page manager groups")
    public PagedList<GroupResponse> pageMyGroupsAsForManager(@RequestBody @Valid MyGroupsAsForManagerPageQuery query,
                                                             @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.pageMyGroupsAsForManager(query, userContext);
    }

    @PostMapping("/page/my-joined")
    @Operation(summary = "page joined groups")
    public PagedList<GroupResponse> pageMyGroupsAsForMember(@RequestBody @Valid MyGroupsAsForMemberPageQuery query,
                                                            @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.pageMyGroupsAsForMember(query, userContext);
    }

    @GetMapping("/permission/admin")
    @Operation(summary = "fetch admin permission")
    public FolderPermissionResponse fetchAdminPermission(@RequestParam @NotBlank String customId,
                                                         @RequestParam @NotBlank String folderId) {
        return groupQueryService.fetchAdminPermission(customId, folderId);
    }

    @GetMapping("/permission/member")
    @Operation(summary = "fetch member permission")
    public FolderPermissionResponse fetchMemberPermission(@RequestParam @NotBlank String customId,
                                                          @RequestParam @NotBlank String folderId,
                                                          @RequestParam(required = false) @Id(USER_ID_PREFIX) String memberId,
                                                          @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.fetchMemberPermission(customId, folderId, memberId, userContext);
    }
}
