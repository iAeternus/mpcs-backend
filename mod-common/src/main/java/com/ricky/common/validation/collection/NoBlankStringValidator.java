package com.ricky.common.validation.collection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

import static com.ricky.common.utils.ValidationUtils.isEmpty;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/24
 * @className NoBlankStringValidator
 * @desc
 */
public class NoBlankStringValidator implements ConstraintValidator<NoBlankString, Collection<String>> {
    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (isEmpty(collection)) {
            return true;
        }

        return collection.stream()
                .noneMatch(StringUtils::isBlank);
    }
}
