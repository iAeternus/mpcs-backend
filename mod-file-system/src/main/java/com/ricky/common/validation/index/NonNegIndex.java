package com.ricky.common.validation.index;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = NonNegIndexValidator.class)
@Documented
public @interface NonNegIndex {

    String message() default "Index must be non negative.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
