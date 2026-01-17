package com.ricky.like.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstants.LIKE_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.LIKE_ID_PREFIX;

/**
 * Like 模块采用 Redis 写模型承载高频行为，
 * 数据库聚合根仅用于状态收敛与事实归档，
 * 查询侧始终以 Redis 为事实源，
 * 系统保证最终一致性而非实时强一致。
 */
@Getter
@Document(LIKE_COLLECTION)
@TypeAlias(LIKE_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Like extends AggregateRoot {

    private String likerId; // 点赞者ID
    private String postId; // 点赞对象ID
    private LikeStatus status;

    public Like(LikeRecord record, UserContext userContext) {
        super(newLikeId(), userContext);
        init(record, userContext);
    }

    private void init(LikeRecord record, UserContext userContext) {
        this.likerId = record.getUserId();
        this.postId = record.getPostId();
        this.status = record.getStatus();
        addOpsLog("点赞", userContext);
    }

    public static String newLikeId() {
        return LIKE_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

}
