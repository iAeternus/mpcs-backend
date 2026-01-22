package com.ricky.apitest;

import com.apifan.common.random.source.AreaSource;
import com.apifan.common.random.source.OtherSource;
import com.apifan.common.random.source.PersonInfoSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;

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

    public static String rFolderName() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "文件夹";
    }

    public static String rFilename() {
        return RandomStringUtils.secure().nextAlphabetic(6) + "文件";
    }

    public static String rGroupName() {
        return rRawGroupName() + RandomStringUtils.secure().nextAlphabetic(10);
    }

    public static String rRawGroupName() {
        return AreaSource.getInstance().randomCity(",").split(",")[1] + OtherSource.getInstance().randomCompanyDepartment();
    }

    public static String rDescription() {
        return rSentence(100);
    }

    public static String rSentence(int maxLength) {
        if (maxLength < 5) {
            return RandomStringUtils.secure().next(maxLength);
        }

        String sentence = OtherSource.getInstance().randomChinese(RandomUtils.secure().randomInt(1, 5000));
        if (sentence.length() > maxLength) {
            return sentence.substring(0, maxLength - 1).trim();
        }

        String trimed = sentence.trim();
        if (isBlank(trimed)) {
            return RandomStringUtils.secure().next(maxLength);
        }

        return trimed;
    }

    public static String rCommentContent() {
        return rSentence(64);
    }

}
