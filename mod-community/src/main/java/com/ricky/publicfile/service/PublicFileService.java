package com.ricky.publicfile.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.publicfile.command.EditDescriptionCommand;
import com.ricky.publicfile.command.ModifyTitleCommand;
import com.ricky.publicfile.command.PostCommand;
import com.ricky.publicfile.command.PostResponse;

public interface PublicFileService {
    PostResponse post(PostCommand command, UserContext userContext);

    void withdraw(String postId, UserContext userContext);

    void updateTitle(ModifyTitleCommand command, UserContext userContext);

    void editDescription(EditDescriptionCommand command, UserContext userContext);
}
