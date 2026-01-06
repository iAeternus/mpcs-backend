package com.ricky.user.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.USER_CREATED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.USER_CREATED;
import static lombok.AccessLevel.PRIVATE;

// TODO 还没想好怎么处理
@Getter
@TypeAlias(USER_CREATED_EVENT_NAME)
@NoArgsConstructor(access = PRIVATE)
public class UserCreatedEvent extends DomainEvent {

    private String userId;

    public UserCreatedEvent(String memberId, UserContext userContext) {
        super(USER_CREATED, userContext);
        this.userId = memberId;
    }

}
