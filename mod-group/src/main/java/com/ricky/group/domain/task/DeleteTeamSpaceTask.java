package com.ricky.group.domain.task;

import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeleteTeamSpaceTask {

    private final FolderRepository folderRepository;

    public void run(String customId) {
        List<Folder> folders = folderRepository.getAllByCustomId(customId);
        folderRepository.delete(folders);
    }

}
