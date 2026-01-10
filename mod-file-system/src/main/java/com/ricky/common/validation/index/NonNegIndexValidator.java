package com.ricky.common.validation.index;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.ricky.common.utils.ValidationUtils.isNull;

public class NonNegIndexValidator implements ConstraintValidator<NonNegIndex, Integer> {

    @Override
    public boolean isValid(Integer index, ConstraintValidatorContext ctx) {
        if (isNull(index)) {
            return true;
        }
        return isNonNegIndex(index);
    }

    public static boolean isNonNegIndex(Integer index) {
        return index >= 0;
    }

}
