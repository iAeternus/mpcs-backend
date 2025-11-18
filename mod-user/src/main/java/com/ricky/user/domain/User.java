package com.ricky.user.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstant.USER_COLLECTION;
import static com.ricky.common.constants.ConfigConstant.USER_ID_PREFIX;

/**
 * @brief 用户
 */
@Getter
@Document(USER_COLLECTION)
@TypeAlias(USER_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends AggregateRoot {

    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;

    public static String newUserId() {
        return USER_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

}
