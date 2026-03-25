package com.ricky.collaboration.command;

import com.ricky.common.validation.id.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionCommand {

    @NotBlank(message = "文档ID不能为空")
    @Id(FILE_ID_PREFIX)
    private String documentId;

    @NotBlank(message = "文档标题不能为空")
    private String documentTitle;

    @Id(FOLDER_ID_PREFIX)
    private String parentFolderId;

    private long ttlHours;
}
