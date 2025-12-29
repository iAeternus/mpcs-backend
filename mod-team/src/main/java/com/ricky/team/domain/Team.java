package com.ricky.team.domain;

import com.ricky.common.domain.AggregateRoot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.TEAM_COLLECTION;

/**
 * @brief 团队
 */
@Getter
@Document(TEAM_COLLECTION)
@TypeAlias(TEAM_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Team extends AggregateRoot {

    private String name;
    private String ownerId; // 团队创建者ID
    private List<Member> members;

    // TODO

}
