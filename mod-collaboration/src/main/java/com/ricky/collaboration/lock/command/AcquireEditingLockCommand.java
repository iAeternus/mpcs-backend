package com.ricky.collaboration.lock.command;

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
public class AcquireEditingLockCommand {

    private String sessionId;

    @NotBlank
    private String documentId;

    @NotNull
    @Min(0)
    private Integer start;

    @NotNull
    @Min(0)
    private Integer end;
}
