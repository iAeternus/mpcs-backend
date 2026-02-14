package com.ricky.upload.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileFactory;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_NAME_DUPLICATES;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class FileUploadDomainService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final FileFactory fileFactory;
    private final FileExtraRepository fileExtraRepository;

    public void checkFilenameDuplicates(String parentId, String filename) {
        Folder parentFolder = folderRepository.cachedById(parentId);
        if (isEmpty(parentFolder.getFileIds())) {
            return;
        }

        List<String> filenames = fileRepository.byIds(parentFolder.getFileIds()).stream()
                .map(File::getFilename)
                .collect(toImmutableList());
        if (filenames.contains(filename)) {
            throw new MyException(FILE_NAME_DUPLICATES, "File name duplicates.",
                    "parentId", parentId, "filename", filename);
        }
    }

    public Optional<File> findExistingFile(String fileHash, String parentId, String filename) {
        List<File> files = fileRepository.listByFileHash(fileHash);
        return files.stream()
                .filter(file -> parentId.equals(file.getParentId()) && filename.equals(file.getFilename()))
                .findFirst();
    }

    public File createFileFromStorageId(String parentId,
                                        String filename,
                                        StorageId storageId,
                                        String fileHash,
                                        long totalSize,
                                        UserContext userContext) {
        File file = fileFactory.create(parentId, filename, storageId, fileHash, totalSize, userContext);
        persistAndAttach(file, parentId, userContext);
        return file;
    }

    public File createFileFromStoredFile(String parentId,
                                         String filename,
                                         StoredFile storedFile,
                                         UserContext userContext) {
        File file = fileFactory.create(parentId, filename, storedFile, userContext);
        persistAndAttach(file, parentId, userContext);
        return file;
    }

    public File createFileFromMultipartFile(String parentId,
                                            StorageId storageId,
                                            MultipartFile file,
                                            String hash,
                                            UserContext userContext) {
        File created = fileFactory.create(parentId, storageId, file, hash, userContext);
        persistAndAttach(created, parentId, userContext);
        return created;
    }

    private void persistAndAttach(File file, String parentId, UserContext userContext) {
        fileRepository.save(file);

        Folder parentFolder = folderRepository.cachedById(parentId);
        parentFolder.addFile(file.getId(), userContext);
        folderRepository.save(parentFolder);

        fileExtraRepository.save(new FileExtra(file.getId(), userContext));
    }
}
