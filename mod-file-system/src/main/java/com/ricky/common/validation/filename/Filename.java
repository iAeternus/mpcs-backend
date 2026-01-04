package com.ricky.common.validation.filename;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = FilenameValidator.class)
@Documented
public @interface Filename {

    String message() default "文件名格式不正确。必须包含扩展名（1-10个字母或数字），且不能包含非法字符（\\ / : * ? \" < > | 等）";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
