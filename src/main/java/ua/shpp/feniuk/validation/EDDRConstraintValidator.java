package ua.shpp.feniuk.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EDDRConstraintValidator implements ConstraintValidator<ValidEDDR, String> {
    private final EDDRValidator validator = new EDDRValidator();

    @Override
    public boolean isValid(String eddr, ConstraintValidatorContext context) {
        return validator.isValid(eddr);
    }
}