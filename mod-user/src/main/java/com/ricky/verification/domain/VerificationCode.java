package com.ricky.verification.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.exception.ErrorCodeEnum.VERIFICATION_CODE_COUNT_OVERFLOW;
import static com.ricky.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.ricky.common.utils.ValidationUtils.isNotBlank;
import static lombok.AccessLevel.PRIVATE;


@Getter
@Document(VERIFICATION_COLLECTION)
@TypeAlias(VERIFICATION_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class VerificationCode extends AggregateRoot {

    /**
     * 手机号或邮箱
     */
    private String mobileOrEmail;

    /**
     * 6位验证码
     */
    private String code;

    /**
     * 验证码用于的类型
     */
    private VerificationCodeType type;

    /**
     * 已经使用的次数，使用次数不能超过3次
     */
    private int usedCount;

    public VerificationCode(String mobileOrEmail, VerificationCodeType type, String userId, UserContext userContext) {
        super(newVerificationCodeId(), isNotBlank(userId) ? userId : NO_USER_ID, userContext);
        this.mobileOrEmail = mobileOrEmail;
        this.code = RandomStringUtils.secure().nextNumeric(6);
        this.type = type;
        this.usedCount = 0;
    }

    public static String newVerificationCodeId() {
        return VERIFICATION_ID_PREFIX + newSnowflakeId();
    }

    public void use() {
        if (usedCount >= 3) {
            throw new MyException(VERIFICATION_CODE_COUNT_OVERFLOW, "验证码已超过可使用次数。");
        }

        this.usedCount++;
    }

}
