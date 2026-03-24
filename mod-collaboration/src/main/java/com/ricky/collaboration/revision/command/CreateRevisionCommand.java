package com.ricky.collaboration.revision.command;

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
public class CreateRevisionCommand {

    @NotBlank
    private String sessionId;

    @NotBlank
    private String documentId;

    @NotBlank
    private String documentTitle;

    @NotNull
    private Long baseVersion;

    @NotBlank
    private String content;

    private String changeSummary;

    @NotBlank
    private String source;
}
