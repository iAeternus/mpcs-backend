package com.ricky.file.domain;

import com.ricky.common.hash.AbstractFileHasher;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.file.infra.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileDomainService {

    private final FileHasherFactory fileHasherFactory;
    private final FileRepository fileRepository;

    public boolean exists(MultipartFile file) {
        AbstractFileHasher fileHasher = fileHasherFactory.getFileHasher();
        try {
            String hash = fileHasher.hash(file.getInputStream());
            return fileRepository.existsByHash(hash);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
