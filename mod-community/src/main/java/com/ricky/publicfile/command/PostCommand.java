package com.ricky.publicfile.command;

import com.ricky.common.domain.marker.Command;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCommand implements Command {

    String fileId;

}
