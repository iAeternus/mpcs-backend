package com.ricky;

import com.apifan.common.random.source.AreaSource;
import com.apifan.common.random.source.OtherSource;
import com.apifan.common.random.source.PersonInfoSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class RandomTestFixture {

    public static String rMobile() {
        return String.valueOf(RandomUtils.secure().randomLong(13000000000L, 19000000000L));
    }

    public static String rEmail() {
        return (PersonInfoSource.getInstance().randomEnglishName().split(" ")[0] + "@" + RandomStringUtils.secure().nextAlphabetic(rInt(3, 8)) + ".com").toLowerCase();
    }

    public static String rMobileOrEmail() {
        return rBool() ? rMobile() : rEmail();
    }

    public static String rPassword() {
        return RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rVerificationCode() {
        return RandomStringUtils.secure().nextNumeric(6);
    }

    public static String rUsername() {
        return rRawUsername() + RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rRawUsername() {
        return PersonInfoSource.getInstance().randomChineseName();
    }

    public static boolean rBool() {
        return RandomUtils.secure().randomBoolean();
    }

    public static int rInt(int minInclusive, int maxInclusive) {
        return RandomUtils.secure().randomInt(minInclusive, maxInclusive + 1);
    }

    public static String rUrl() {
        return "https://www." + RandomStringUtils.secure().nextAlphabetic(10) + ".com";
    }


}
