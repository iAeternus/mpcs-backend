package com.ricky.collaboration.collaboration.exception;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;

public class VersionConflictException extends MyException {
    
    public VersionConflictException(long clientVersion, long serverVersion) {
        super(ErrorCodeEnum.COLLAB_VERSION_CONFLICT, "版本冲突，请同步最新内容。", 
                "clientVersion", clientVersion, "serverVersion", serverVersion);
    }
}
