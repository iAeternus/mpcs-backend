package com.ricky.common.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.ricky.common.constants.RegexConstants.EMAIL_PATTERN;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/9
 * @className EmailValidator
 * @desc
 */
public class EmailValidator implements ConstraintValidator<Email, String> {

    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN, CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value)) {
            return true;
        }

        return value.length() <= 50 && PATTERN.matcher(value).matches();
    }
}
