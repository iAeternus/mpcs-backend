package com.ricky.publicfile.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.publicfile.command.EditDescriptionCommand;
import com.ricky.publicfile.command.ModifyTitleCommand;
import com.ricky.publicfile.command.PostCommand;
import com.ricky.publicfile.command.PostResponse;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import com.ricky.publicfile.service.PublicFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicFileServiceImpl implements PublicFileService {

    private final RateLimiter rateLimiter;
    private final PublicFileRepository publicFileRepository;
    private final FileRepository fileRepository;

    @Override
    @Transactional
    public PostResponse post(PostCommand command, UserContext userContext) {
        rateLimiter.applyFor("PublicFile:Post", 10);

        File file = fileRepository.cachedById(command.getFileId());
        PublicFile publicFile = new PublicFile(file, userContext);
        publicFileRepository.save(publicFile);

        log.info("Posted File[{}] via PostFile[{}]", command.getFileId(), publicFile.getId());
        return PostResponse.builder()
                .postId(publicFile.getId())
                .build();
    }

    @Override
    @Transactional
    public void withdraw(String postId, UserContext userContext) {
        rateLimiter.applyFor("PublicFile:Withdraw", 10);

        PublicFile publicFile = publicFileRepository.byId(postId);
        publicFile.onDelete(userContext);
        publicFileRepository.delete(publicFile);

        log.info("Withdrew PublicFile[{}]", publicFile.getId());
    }

    @Override
    @Transactional
    public void updateTitle(ModifyTitleCommand command, UserContext userContext) {
        rateLimiter.applyFor("PublicFile:ModifyTitle", 10);

        PublicFile publicFile = publicFileRepository.byId(command.getPostId());
        publicFile.updateTitle(command.getNewTitle(), userContext);
        publicFileRepository.save(publicFile);

        log.info("Modified title of PublicFile[{}]", publicFile.getId());
    }

    @Override
    public void editDescription(EditDescriptionCommand command, UserContext userContext) {
        rateLimiter.applyFor("PublicFile:EditDescription", 10);

        PublicFile publicFile = publicFileRepository.byId(command.getPostId());
        publicFile.updateDescription(command.getNewDescription(), userContext);
        publicFileRepository.save(publicFile);

        log.info("Edited description of PublicFile[{}]", publicFile.getId());
    }
}
