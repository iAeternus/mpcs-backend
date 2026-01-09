package com.ricky.common.validation.id.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.ricky.common.constants.ConfigConstants.MAX_CUSTOM_ID_LENGTH;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class CustomIdValidator implements ConstraintValidator<CustomId, String> {

    @Override
    public void initialize(CustomId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String customId, ConstraintValidatorContext context) {
        if (isBlank(customId)) {
            return true;
        }

        if (customId.contains(" ")) {
            return false;
        }

        return customId.length() <= MAX_CUSTOM_ID_LENGTH;
    }


}
