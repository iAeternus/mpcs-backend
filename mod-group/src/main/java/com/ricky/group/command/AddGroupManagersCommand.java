package com.ricky.group.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.collection.NoBlankString;
import com.ricky.common.validation.collection.NoDuplicatedString;
import com.ricky.common.validation.id.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.USER_ID_PREFIX;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddGroupManagersCommand implements Command {

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = 1000)
    List<@Id(USER_ID_PREFIX) String> managerIds;

}
