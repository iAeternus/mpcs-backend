package com.ricky.collaboration.collaboration.exception;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;

public class SessionAlreadyExistsException extends MyException {
    
    public SessionAlreadyExistsException(String documentId) {
        super(ErrorCodeEnum.COLLAB_SESSION_ALREADY_EXISTS, "文档已存在协同会话。", "documentId", documentId);
    }
}
