package com.ricky.common.llm;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.exception.MyException;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static com.ricky.common.exception.ErrorCodeEnum.LLM_CALL_PARAMS_INVALID;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AICommand implements Command {

    @NotBlank
    String userPrompt; // 用户问题
    String systemPrompt; // 系统指令

    @Override
    public void correctAndValidate() {
        Command.super.correctAndValidate();
        if (isBlank(userPrompt)) {
            throw new MyException(LLM_CALL_PARAMS_INVALID, "userPrompt不能为空");
        }
    }
}
