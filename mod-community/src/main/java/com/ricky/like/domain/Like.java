package com.ricky.like.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.like.domain.event.LikedEvent;
import com.ricky.like.domain.event.UnlikedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstants.LIKE_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.LIKE_ID_PREFIX;

@Getter
@Document(LIKE_COLLECTION)
@TypeAlias(LIKE_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Like extends AggregateRoot {

    private String postId;
    private boolean liked;

    public Like(String postId, UserContext userContext) {
        super(newLikeId(), userContext);
        this.postId = postId;
        this.liked = true;
    }

    private void init(String  postId, UserContext userContext) {
        this.postId = postId;
        raiseEvent(new LikedEvent(getUserId(), getPostId(), userContext));
        addOpsLog("点赞", userContext);
    }

    public static String newLikeId() {
        return LIKE_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void unlike(UserContext userContext) {
        if(!liked) {
            return;
        }

        this.liked = false;
        raiseEvent(new UnlikedEvent(getUserId(), getPostId(), userContext));
        addOpsLog("取消点赞", userContext);
    }

    public void like(UserContext userContext) {
        if(liked) {
            return;
        }

        this.liked = true;
        raiseEvent(new LikedEvent(getUserId(), getPostId(), userContext));
        addOpsLog("恢复点赞", userContext);
    }

}
