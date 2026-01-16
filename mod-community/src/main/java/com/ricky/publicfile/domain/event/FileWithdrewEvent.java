package com.ricky.publicfile.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.FILE_WITHDREW_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FILE_WITHDREW;

@Getter
@TypeAlias(FILE_WITHDREW_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileWithdrewEvent extends DomainEvent {

    private String postId;

    public FileWithdrewEvent(String postId, UserContext userContext) {
        super(FILE_WITHDREW, userContext);
        this.postId = postId;
    }

}
