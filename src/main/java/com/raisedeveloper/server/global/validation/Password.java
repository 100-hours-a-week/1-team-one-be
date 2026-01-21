package com.raisedeveloper.server.global.validation;

import static com.raisedeveloper.server.domain.common.ValidationConstants.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {

	String message() default PASSWORD_FORMAT_INVALID;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
