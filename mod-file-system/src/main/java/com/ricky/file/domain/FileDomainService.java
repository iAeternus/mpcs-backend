package com.ricky.file.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.upload.domain.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.isNotEmpty;

@Service
@RequiredArgsConstructor
public class FileDomainService {

    private final FileStorage fileStorage;
    private final FileRepository fileRepository;
    private final FileExtraRepository fileExtraRepository;

    // 注意deleteFileForce和deleteFilesForce逻辑并不一样
    // 前者需要调用者先删除File聚合根，listByStorageId将查询出空集合，表明无聚合根指向文件内容，才能级联删除文件内容
    // 后者的调用者不能删除File聚合根，若listByStorageIds查询出空集合，则不会有存储ID可以删
    public void deleteFileForce(File file, UserContext userContext) {
        List<File> files = fileRepository.listByStorageId(file.getStorageId());
        if (isEmpty(files)) {
            fileStorage.delete(file.getStorageId());
        }

        FileExtra fileExtra = fileExtraRepository.byFileId(file.getId());
        fileExtra.onDelete(userContext);
        fileExtraRepository.delete(fileExtra);
    }

    public void deleteFilesForce(List<File> files, UserContext userContext) {
        List<StorageId> storageIds = files.stream().map(File::getStorageId).collect(toImmutableList());
        Map<StorageId, List<File>> storageIdFiles = fileRepository.listByStorageIds(storageIds);

        List<StorageId> emptyStorageIds = storageIdFiles.entrySet().stream()
                .filter(entry -> entry.getValue().size() == 1)
                .map(Map.Entry::getKey)
                .collect(toImmutableList());

        // 批量删除聚合根
        fileRepository.delete(files);

        // 若删除文件聚合根后无聚合根指向文件内容，则级联删除文件内容
        if (isNotEmpty(emptyStorageIds)) {
            fileStorage.delete(emptyStorageIds);
        }

        List<String> fileIds = files.stream().map(File::getId).collect(toImmutableList());
        List<FileExtra> fileExtras = fileExtraRepository.listByFileIds(fileIds);
        fileExtras.forEach(fileExtra -> fileExtra.onDelete(userContext));
        fileExtraRepository.delete(fileExtras);
    }
}
