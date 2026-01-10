package com.ricky.common.validation.path;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.ricky.common.constants.RegexConstants.PATH_PATTERN;
import static com.ricky.common.utils.ValidationUtils.isBlank;

public class PathValidator implements ConstraintValidator<Path, String> {

    private static final Pattern PATTERN = Pattern.compile(PATH_PATTERN);

    @Override
    public boolean isValid(String path, ConstraintValidatorContext ctx) {
        if (isBlank(path)) {
            return true;
        }

        return isPath(path);
    }

    public static boolean isPath(String path) {
        return PATTERN.matcher(path).matches();
    }

}
