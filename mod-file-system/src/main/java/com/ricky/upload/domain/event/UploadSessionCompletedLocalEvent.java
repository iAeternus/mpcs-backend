package com.ricky.upload.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.LocalDomainEvent;
import com.ricky.upload.domain.UploadSession;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadSessionCompletedLocalEvent extends LocalDomainEvent {

    private String uploadId;

    public UploadSessionCompletedLocalEvent(UploadSession uploadSession, String uploadId, UserContext userContext) {
        super(uploadSession, userContext);
        this.uploadId = uploadId;
    }

}
