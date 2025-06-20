package ua.shpp.feniuk.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EDDRConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEDDR {
    String message() default "Invalid EDDR";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
