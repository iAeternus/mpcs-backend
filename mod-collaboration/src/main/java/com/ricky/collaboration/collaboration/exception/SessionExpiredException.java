package com.ricky.collaboration.collaboration.exception;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;

public class SessionExpiredException extends MyException {
    
    public SessionExpiredException(String sessionId) {
        super(ErrorCodeEnum.COLLAB_SESSION_EXPIRED, "协同会话已过期。", "sessionId", sessionId);
    }
}
