package com.ricky.common.validation.filename;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.ricky.common.constants.RegexConstants.FILENAME_PATTERN;
import static com.ricky.common.utils.ValidationUtils.isBlank;

public class FilenameValidator implements ConstraintValidator<Filename, String> {

    private static final Pattern PATTERN = Pattern.compile(FILENAME_PATTERN);

    @Override
    public boolean isValid(String filename, ConstraintValidatorContext ctx) {
        if (isBlank(filename)) {
            return true;
        }

        return isFilename(filename);
    }

    public static boolean isFilename(String filename) {
        return PATTERN.matcher(filename).matches();
    }
}
