package com.ricky.common.domain;

import com.ricky.common.utils.ValidationUtils;
import lombok.Getter;

import static com.ricky.management.MpcsManageUser.MPCS_CUSTOM_ID_POSTFIX;

/**
 * 空间划分，用于标识文件夹和文件夹层次结构<br>
 * 每个空间都有专属且唯一的文件夹层次结构<br>
 * 用户 一对一 个人空间，用户 一对多 团队空间，公共空间全系统唯一
 */
@Getter
public enum SpaceType {

    PERSONAL("个人空间", "PERSONAL"),
    TEAM("团队空间", "TEAM"),
    PUBLIC("公共空间", "PUBLIC"),
    ;

    private final String desc;
    private final String prefix;

    SpaceType(String desc, String prefix) {
        this.desc = desc;
        this.prefix = prefix;
    }


    public static String personalCustomId(String userId) {
        return toCustomId(PERSONAL, userId);
    }

    public static String teamCustomId(String groupId) {
        return toCustomId(TEAM, groupId);
    }

    public static String publicCustomId() {
        return toCustomId(PUBLIC, MPCS_CUSTOM_ID_POSTFIX);
    }

    /**
     * 转化为 customId
     *
     * @param spaceType 空间类型
     * @param postfix   后缀
     *                  1. 公共空间: MPCS_CUSTOM_ID_POSTFIX
     *                  2. 个人空间: userId
     *                  3. 团队空间: groupId
     * @return customId
     */
    private static String toCustomId(SpaceType spaceType, String postfix) {
        return spaceType.prefix + "-" + postfix;
    }

    public static SpaceType fromCustomId(String customId) {
        return fromPrefix(customId.split("-")[0]);
    }

    private static SpaceType fromPrefix(String prefix) {
        if (ValidationUtils.equals(prefix, PERSONAL.getPrefix())) {
            return PERSONAL;
        } else if (ValidationUtils.equals(prefix, TEAM.getPrefix())) {
            return TEAM;
        } else if (ValidationUtils.equals(prefix, PUBLIC.getPrefix())) {
            return PUBLIC;
        }
        throw new IllegalStateException("非法的customId前缀：" + prefix);
    }
}
