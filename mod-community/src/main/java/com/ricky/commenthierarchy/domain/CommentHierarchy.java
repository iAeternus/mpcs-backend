package com.ricky.commenthierarchy.domain;

import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.domain.Comment;
import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.idtree.IdTreeHierarchy;
import com.ricky.common.domain.idtree.exception.IdNodeLevelOverflowException;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.common.validation.id.Id;
import com.ricky.folderhierarchy.domain.event.FolderHierarchyChangedEvent;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(COMMENT_HIERARCHY_COLLECTION)
@TypeAlias(COMMENT_HIERARCHY_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentHierarchy extends AggregateRoot {

    private String postId; // 发布物ID
    private IdTree idTree;
    private IdTreeHierarchy hierarchy;

    public CommentHierarchy(String postId, UserContext userContext) {
        super(newCommentHierarchyId(), userContext);
        init(postId, userContext);
    }

    private void init(String postId, UserContext userContext) {
        this.postId = postId;
        this.idTree = new IdTree(new ArrayList<>(0));
        this.buildHierarchy();
        addOpsLog("新建", userContext);
    }

    public static String newCommentHierarchyId() {
        return COMMENT_HIERARCHY_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    private void buildHierarchy() {
        try {
            this.hierarchy = this.idTree.buildHierarchy(MAX_COMMENT_HIERARCHY_LEVEL);
        } catch (IdNodeLevelOverflowException ex) {
            String msg = "评论层级最多不能超过" + MAX_COMMENT_HIERARCHY_LEVEL + "层。";
            throw new MyException(COMMENT_HIERARCHY_TOO_DEEP, msg, "postId", this.getPostId());
        }
    }

    public void addComment(Comment comment, String parentId, UserContext userContext) {
        String commentId = comment.getId();
        if (isNotBlank(parentId)) {
            if (!containsCommentId(parentId)) {
                throw new MyException(COMMENT_NOT_FOUND, "未找到评论。",
                        "parentId", parentId, "commentId", commentId);
            }

            if (this.hierarchy.levelOf(parentId) >= MAX_COMMENT_HIERARCHY_LEVEL) {
                String msg = "添加失败，评论层级最多不能超过" + MAX_COMMENT_HIERARCHY_LEVEL + "层。";
                throw new MyException(COMMENT_HIERARCHY_TOO_DEEP, msg, "userId", this.getUserId());
            }
        }

        this.idTree.addNode(parentId, commentId);
        this.buildHierarchy();
        addOpsLog("添加评论[" + commentId + "]", userContext);
    }

    public boolean containsCommentId(String commentId) {
        return this.hierarchy.containsId(commentId);
    }

    public void removeComment(String commentId, UserContext userContext) {
        this.idTree.removeNode(commentId);
        this.buildHierarchy();
        addOpsLog("删除评论[" + commentId + "]", userContext);
    }

    public Set<String> withAllChildIdsOf(String commentId) {
        return this.hierarchy.withAllParentIdsOf(commentId);
    }
}
