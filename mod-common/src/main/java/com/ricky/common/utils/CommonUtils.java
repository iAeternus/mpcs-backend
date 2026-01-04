package com.ricky.common.utils;

import com.ricky.common.constants.RegexConstants;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;

import static java.util.regex.Pattern.matches;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class CommonUtils {

    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    /**
     * @brief 从给定的类中提取其父类的单个泛型参数的实际类型
     */
    public static Class<?> singleParameterizedArgumentClassOf(Class<?> aClass) {
        // The aClass might be proxied by Spring CGlib, so we need to get the real targeted class
        Class<?> realClass = aClass.getName().contains(CGLIB_CLASS_SEPARATOR) ? aClass.getSuperclass() : aClass;

        Type genericSuperclass = realClass.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return null;
        }

        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();

        if (actualTypeArguments.length != 1) {
            throw new RuntimeException("Expecting exactly one parameterized type argument for " + realClass);
        }

        Type actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
            return (Class<?>) actualTypeArgument;
        }
        return null;
    }

    public static boolean isMobileNumber(String value) {
        return matches(RegexConstants.MOBILE_PATTERN, value);
    }

    public static boolean isEmail(String value) {
        return matches(RegexConstants.EMAIL_PATTERN, value);
    }

    public static String maskMobileOrEmail(String mobileOrEmail) {
        if (isBlank(mobileOrEmail)) {
            return mobileOrEmail;
        }

        if (isMobileNumber(mobileOrEmail)) {
            return mobileOrEmail.replaceAll("(\\w{3})\\w*(\\w{4})", "$1****$2");
        }

        return mobileOrEmail.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
    }

    public static String maskMobile(String mobile) {
        if (isBlank(mobile)) {
            return mobile;
        }

        return mobile.replaceAll("(\\w{3})\\w*(\\w{4})", "$1****$2");
    }

    public static String redisCacheKey(String cacheName, String userId) {
        return "Cache:" + cacheName + "::" + userId;
    }

}
