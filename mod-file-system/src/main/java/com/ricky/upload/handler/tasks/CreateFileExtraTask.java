package com.ricky.upload.handler.tasks;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.domain.user.UserContext;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateFileExtraTask implements RetryableTask {

    private final FileExtraRepository fileExtraRepository;

    public void run(String fileId, UserContext userContext) {
        FileExtra fileExtra = new FileExtra(fileId, userContext);
        fileExtraRepository.save(fileExtra);
    }

}
