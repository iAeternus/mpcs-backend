package com.ricky.group;

import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.group.command.*;
import com.ricky.group.query.*;
import com.ricky.group.service.GroupQueryService;
import com.ricky.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.GROUP_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.USER_ID_PREFIX;
import static com.ricky.common.domain.dto.resp.IdResponse.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@CrossOrigin
@RestController
@Tag(name = "权限组模块")
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final GroupQueryService groupQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "创建权限组")
    public IdResponse createGroup(@RequestBody @Valid CreateGroupCommand command,
                                  @AuthenticationPrincipal UserContext userContext) {
        String groupId = groupService.createGroup(command, userContext);
        return returnId(groupId);
    }

    @PutMapping("/{groupId}/name")
    @Operation(summary = "重命名权限组")
    public void renameGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                            @RequestBody @Valid RenameGroupCommand command,
                            @AuthenticationPrincipal UserContext userContext) {
        groupService.renameGroup(groupId, command, userContext);
    }

    @PutMapping("/{groupId}/members")
    @Operation(summary = "批量添加组成员")
    public void addGroupMembers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                @RequestBody @Valid AddGroupMembersCommand command,
                                @AuthenticationPrincipal UserContext userContext) {
        groupService.addGroupMembers(groupId, command, userContext);
    }

    @PutMapping("/{groupId}/managers")
    @Operation(summary = "批量添加组管理员")
    public void addGroupManagers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                 @RequestBody @Valid AddGroupManagersCommand command,
                                 @AuthenticationPrincipal UserContext userContext) {
        groupService.addGroupManagers(groupId, command, userContext);
    }

    @Operation(summary = "删除组成员")
    @DeleteMapping("/{groupId}/members/{memberId}")
    public void removeGroupMember(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                  @PathVariable @NotBlank @Id(USER_ID_PREFIX) String memberId,
                                  @AuthenticationPrincipal UserContext userContext) {
        groupService.removeGroupMember(groupId, memberId, userContext);
    }

    @Operation(summary = "添加组管理员")
    @PutMapping("/{groupId}/managers/{memberId}")
    public void addGroupManager(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                @PathVariable @NotBlank @Id(USER_ID_PREFIX) String memberId,
                                @AuthenticationPrincipal UserContext userContext) {
        groupService.addGroupManager(groupId, memberId, userContext);
    }

    @Operation(summary = "删除组管理员")
    @DeleteMapping("/{groupId}/managers/{memberId}")
    public void removeGroupManager(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                   @PathVariable @NotBlank @Id(USER_ID_PREFIX) String memberId,
                                   @AuthenticationPrincipal UserContext userContext) {
        groupService.removeGroupManager(groupId, memberId, userContext);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "删除权限组")
    public void deleteGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                            @AuthenticationPrincipal UserContext userContext) {
        groupService.deleteGroup(groupId, userContext);
    }

    @Operation(summary = "启用权限组")
    @PutMapping("/{groupId}/activation")
    public void activateGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                              @AuthenticationPrincipal UserContext userContext) {
        groupService.activateGroup(groupId, userContext);
    }

    @Operation(summary = "禁用权限组")
    @PutMapping("/{groupId}/deactivation")
    public void deactivateGroup(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                @AuthenticationPrincipal UserContext userContext) {
        groupService.deactivateGroup(groupId, userContext);
    }

    @PutMapping("/grant")
    @Operation(summary = "添加文件夹并指定权限集合")
    public void addGrant(@RequestBody @Valid AddGrantCommand command,
                         @AuthenticationPrincipal UserContext userContext) {
        groupService.addGrant(command, userContext);
    }

    @PutMapping("/grants")
    @Operation(summary = "批量添加文件夹并指定权限集合")
    public void addGrants(@RequestBody @Valid AddGrantsCommand command,
                          @AuthenticationPrincipal UserContext userContext) {
        groupService.addGrants(command, userContext);
    }

    @GetMapping("/{groupId}/folders")
    @Operation(summary = "获取权限组管理的文件夹")
    public GroupFoldersResponse fetchGroupFolders(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId) {
        return groupQueryService.fetchGroupFolders(groupId);
    }

    @GetMapping("/{groupId}/ordinary-member")
    @Operation(summary = "获取权限组普通成员列表")
    public GroupOrdinaryMembersResponse fetchGroupOrdinaryMembers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                                                  @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.fetchGroupOrdinaryMembers(groupId, userContext);
    }

    @GetMapping("/{groupId}/managers")
    @Operation(summary = "获取权限组管理员列表")
    public GroupManagersResponse fetchGroupManagers(@PathVariable @NotBlank @Id(GROUP_ID_PREFIX) String groupId,
                                                    @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.fetchGroupManagers(groupId, userContext);
    }

    @PostMapping("/page/my-groups")
    @Operation(summary = "分页获取我管理的权限组")
    public PagedList<GroupResponse> pageMyGroupsAsForManager(@RequestBody @Valid MyGroupsAsForManaberPageQuery query,
                                                             @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.pageMyGroupsAsForManager(query, userContext);
    }

    @PostMapping("/page/my-joined")
    @Operation(summary = "分页获取我加入的权限组（包括管理员）")
    public PagedList<GroupResponse> pageMyGroupsAsForMember(@RequestBody @Valid MyGroupsAsForMemberPageQuery query,
                                                            @AuthenticationPrincipal UserContext userContext) {
        return groupQueryService.pageMyGroupsAsForMember(query, userContext);
    }

}
