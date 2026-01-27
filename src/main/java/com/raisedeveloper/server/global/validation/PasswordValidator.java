package com.raisedeveloper.server.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}

		boolean hasLower = value.chars().anyMatch(Character::isLowerCase);
		boolean hasUpper = value.chars().anyMatch(Character::isUpperCase);
		boolean hasDigit = value.chars().anyMatch(Character::isDigit);
		boolean hasSpecial = value.chars().anyMatch(ch ->
			!Character.isLetterOrDigit(ch)
		);

		return hasLower && hasUpper && hasDigit && hasSpecial;
	}
}

