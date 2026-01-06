package com.ricky.upload.domain;

import com.ricky.common.exception.MyException;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_NAME_DUPLICATES;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class FileUploadDomainService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    public void checkFilenameDuplicates(String parentId, String filename) {
        Folder parentFolder = folderRepository.cachedById(parentId);
        if (isEmpty(parentFolder.getFileIds())) {
            return;
        }

        List<String> filenames = fileRepository.byIds(parentFolder.getFileIds()).stream()
                .map(File::getFilename)
                .collect(toImmutableList());
        if (filenames.contains(filename)) {
            throw new MyException(FILE_NAME_DUPLICATES, "文件名不能重复。", "filename", filename);
        }
    }
}
