package com.ricky.publicfile.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.FILE_PUBLISHED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FILE_PUBLISHED;

@Getter
@TypeAlias(FILE_PUBLISHED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilePublishedEvent extends DomainEvent {

    private String originalFileId;
    private String postId;

    public FilePublishedEvent(String originalFileId, String postId, UserContext userContext) {
        super(FILE_PUBLISHED, userContext);
        this.originalFileId = originalFileId;
        this.postId = postId;
    }

}
