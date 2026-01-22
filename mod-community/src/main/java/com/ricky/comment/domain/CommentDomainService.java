package com.ricky.comment.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ricky.common.exception.ErrorCodeEnum.PUBLIC_FILE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CommentDomainService {

    private final PublicFileRepository publicFileRepository;

    public void checkPostIdExists(String postId, UserContext userContext) {
        if (!publicFileRepository.exists(postId)) {
            throw new MyException(PUBLIC_FILE_NOT_FOUND,
                    "发布物不存在", "postId", postId, "userId", userContext.getUid());
        }
    }

}
