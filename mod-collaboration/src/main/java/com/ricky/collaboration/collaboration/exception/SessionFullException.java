package com.ricky.collaboration.collaboration.exception;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;

public class SessionFullException extends MyException {
    
    public SessionFullException() {
        super(ErrorCodeEnum.COLLAB_SESSION_FULL, "协同会话人数已满。");
    }
}
