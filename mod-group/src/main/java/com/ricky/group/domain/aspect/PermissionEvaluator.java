package com.ricky.group.domain.aspect;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.group.domain.GroupDomainService;
import com.ricky.group.domain.permission.FilePermissionResource;
import com.ricky.group.domain.permission.FolderPermissionResource;
import com.ricky.group.domain.permission.PermissionMetadata;
import com.ricky.group.domain.permission.PermissionResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionEvaluator {

    private final GroupDomainService groupDomainService;
    private final FolderRepository folderRepository;

    public boolean allowed(UserContext user, PermissionMetadata metadata, PermissionResource resource) {
        if (resource instanceof FolderPermissionResource folder) {
            return groupDomainService.hasPermission(
                    user.getUid(),
                    folder.getCustomId(),
                    folder.getFolderId(),
                    metadata.getRequired()
            );
        }

        if (resource instanceof FilePermissionResource file) {
            Folder folder = folderRepository.byFileId(file.getFileId());
            return groupDomainService.hasPermission(
                    user.getUid(),
                    folder.getCustomId(),
                    folder.getId(),
                    metadata.getRequired()
            );
        }

        return false;
    }
}
