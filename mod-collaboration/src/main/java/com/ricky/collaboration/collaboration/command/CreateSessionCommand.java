package com.ricky.collaboration.collaboration.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionCommand {
    
    @NotBlank(message = "文档ID不能为空")
    private String documentId;
    
    @NotBlank(message = "文档标题不能为空")
    private String documentTitle;
    
    private long ttlHours;
}
