package com.ricky.publicfile.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ricky.common.exception.ErrorCodeEnum.PUBLIC_FILE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PublicFileDomainService {

    private final PublicFileRepository publicFileRepository;

    public void checkExists(String postId, UserContext userContext) {
        if (!publicFileRepository.exists(postId)) {
            throw new MyException(PUBLIC_FILE_NOT_FOUND,
                    "发布物不存在", "postId", postId, "userId", userContext.getUid());
        }
    }

}
