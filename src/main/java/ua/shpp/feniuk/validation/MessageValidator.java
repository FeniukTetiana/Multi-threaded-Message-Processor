package ua.shpp.feniuk.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import ua.shpp.feniuk.pojo.PojoMessage;

import java.util.Set;
import java.util.stream.Collectors;

public class MessageValidator {
    private final Validator validator;

    public MessageValidator() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    public boolean isNoConfirmedErrors(PojoMessage pojoMessage) {
        Set<ConstraintViolation<PojoMessage>> violations = validator.validate(pojoMessage);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            pojoMessage.setErrors(errors);
        }
        return violations.isEmpty();
    }
}
