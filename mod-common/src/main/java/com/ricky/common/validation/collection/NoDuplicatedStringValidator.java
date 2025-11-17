package com.ricky.common.validation.collection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/24
 * @className NoDuplicatedStringValidator
 * @desc
 */
public class NoDuplicatedStringValidator implements ConstraintValidator<NoDuplicatedString, Collection<String>> {
    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (isEmpty(collection)) {
            return true;
        }

        return distinct(collection).size() == collection.size();
    }

    private List<String> distinct(Collection<String> collection) {
        return collection.stream()
                .distinct()
                .collect(toImmutableList());
    }
}
