package com.ricky.collaboration.collaboration.exception;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;

public class SessionNotFoundException extends MyException {
    
    public SessionNotFoundException(String sessionId) {
        super(ErrorCodeEnum.COLLAB_SESSION_NOT_FOUND, "协同会话不存在。", "sessionId", sessionId);
    }
}
