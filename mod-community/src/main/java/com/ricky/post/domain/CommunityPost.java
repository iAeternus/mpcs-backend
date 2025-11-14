package com.ricky.post.domain;

import com.ricky.common.domain.AggregateRoot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstant.COMMUNITY_POST_COLLECTION;

/**
 * @brief 社区文件发布记录
 * @note 文件本体仍然由File聚合管理，这里只是发布行为的`投影聚合`
 * @note 社区文件 = 原文件 + CommunityPost
 */
@Getter
@Document(COMMUNITY_POST_COLLECTION)
@TypeAlias(COMMUNITY_POST_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityPost extends AggregateRoot {

    private String fileId; // 对应的文件ID
    private String publisherId; // 发布者ID
    private String title; // 展示标题，默认文件名
    private String description; // 介绍文字
    private Integer likeCount; // 点赞数
    private Integer commentCount; // 评论数

    // TODO

}
