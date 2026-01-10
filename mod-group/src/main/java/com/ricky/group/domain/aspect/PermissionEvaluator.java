package com.ricky.group.domain.aspect;

import com.ricky.common.domain.SpaceType;
import com.ricky.common.domain.user.UserContext;
import com.ricky.group.domain.GroupDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ricky.common.domain.SpaceType.fromCustomId;
import static com.ricky.common.utils.CommonUtils.objectToListString;

@Component
@RequiredArgsConstructor
public class PermissionEvaluator {

    private final GroupDomainService groupDomainService;

    public boolean allowed(UserContext user, PermissionMetadata metadata, List<Object> resources) {
        String customId = resources.get(0).toString();
        SpaceType spaceType = fromCustomId(customId);

        if (spaceType == SpaceType.PERSONAL || spaceType == SpaceType.PUBLIC) {
            return true;
        }

        if (metadata.batch()) {
            List<String> folderIds = objectToListString(resources.get(1));
            return groupDomainService.hasPermission(user.getUid(), customId, folderIds, metadata.required());
        }

        String folderId = resources.get(1).toString();
        return groupDomainService.hasPermission(user.getUid(), customId, folderId, metadata.required());
    }
}
