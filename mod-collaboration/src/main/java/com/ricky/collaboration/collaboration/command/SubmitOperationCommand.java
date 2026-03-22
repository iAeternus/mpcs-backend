package com.ricky.collaboration.collaboration.command;

import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitOperationCommand {
    
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    
    @NotNull(message = "操作类型不能为空")
    private TextOperationType type;
    
    @NotNull(message = "位置不能为空")
    @Min(value = 0, message = "位置必须大于等于0")
    private Integer position;
    
    private String content;
    
    @Min(value = 0, message = "长度必须大于等于0")
    private Integer length;
    
    @NotNull(message = "客户端版本不能为空")
    private Long clientVersion;
}
