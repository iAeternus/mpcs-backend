package com.ricky.common.validation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.ricky.common.constants.RegexConstant.PASSWORD_PATTERN;
import static com.ricky.common.utils.ValidationUtil.isBlank;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/24
 * @className PasswordValidator
 * @desc
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (isBlank(value)) {
            return true;
        }

        return PATTERN.matcher(value).matches();
    }
}
