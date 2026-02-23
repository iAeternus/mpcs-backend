package com.ricky.comment.domain;

import com.ricky.common.domain.hierarchy.HierarchyDomainService;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ricky.common.utils.ValidationUtils.isEmpty;

@Service
public class CommentDomainService extends HierarchyDomainService<Comment, CommentRepository> {

    @Autowired
    public CommentDomainService(CommentRepository repository) {
        super(repository);
    }

    public void deleteComment(String postId, String commentId, UserContext userContext) {
        String path = repository.byIdOptional(postId, commentId)
                .map(comment -> {
                    if (!userContext.getUid().equals(comment.getUserId())) {
                        throw MyException.accessDeniedException("只能删除自己发布的评论。");
                    }
                    return comment.getPath();
                })
                .orElse("");

        List<Comment> subtree = repository.getSubtree(postId, path);
        if (isEmpty(subtree)) {
            return;
        }

        subtree.forEach(c -> c.onDelete(userContext));
        repository.delete(subtree);
    }

}
