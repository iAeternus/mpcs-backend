package com.ricky.group;

import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.common.domain.user.UserContext;
import com.ricky.group.command.CreateGroupCommand;
import com.ricky.group.service.GroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @ResponseStatus(CREATED)
    public IdResponse createGroup(@RequestBody @Valid CreateGroupCommand command,
                                  @AuthenticationPrincipal UserContext userContext) {
        String groupId = groupService.createGroup(command, userContext);
        return returnId(groupId);
    }

}
