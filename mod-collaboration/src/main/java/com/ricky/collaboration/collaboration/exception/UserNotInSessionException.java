package com.ricky.collaboration.collaboration.exception;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;

public class UserNotInSessionException extends MyException {
    
    public UserNotInSessionException(String oderId, String sessionId) {
        super(ErrorCodeEnum.COLLAB_USER_NOT_IN_SESSION, "用户不在协同会话中。", "userId", oderId, "sessionId", sessionId);
    }
}
