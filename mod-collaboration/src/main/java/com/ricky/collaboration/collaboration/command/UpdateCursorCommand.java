package com.ricky.collaboration.collaboration.command;

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
public class UpdateCursorCommand {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotNull(message = "位置不能为空")
    @Min(value = 0, message = "位置必须大于等于0")
    private Integer position;

    private Integer selectionStart;

    private Integer selectionEnd;
}
