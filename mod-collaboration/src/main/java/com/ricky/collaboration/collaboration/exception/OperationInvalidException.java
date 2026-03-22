package com.ricky.collaboration.collaboration.exception;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;

public class OperationInvalidException extends MyException {
    
    public OperationInvalidException(String reason) {
        super(ErrorCodeEnum.COLLAB_OPERATION_INVALID, "无效的操作: " + reason);
    }
}
