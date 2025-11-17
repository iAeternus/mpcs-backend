package com.ricky.common.validation.mobile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.ricky.common.constants.RegexConstant.MOBILE_PATTERN;
import static com.ricky.common.utils.ValidationUtils.isBlank;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/9
 * @className MobileNumberValidator
 * @desc
 */
public class MobileNumberValidator implements ConstraintValidator<Mobile, String> {

    private static final Pattern PATTERN = Pattern.compile(MOBILE_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value)) {
            return true;
        }

        return PATTERN.matcher(value).matches();
    }
}
