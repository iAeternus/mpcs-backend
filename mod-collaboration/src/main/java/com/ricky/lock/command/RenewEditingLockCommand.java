package com.ricky.lock.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewEditingLockCommand {

    @NotBlank
    private String sessionId;

    @NotBlank
    private String lockId;
}
