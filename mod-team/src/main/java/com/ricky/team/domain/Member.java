package com.ricky.team.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

/**
 * @brief 团队成员
 */
@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Member {

    String userId;
    MemberRole role; // 成员角色
    LocalDateTime joinedAt; // 加入时间

    // TODO

}
