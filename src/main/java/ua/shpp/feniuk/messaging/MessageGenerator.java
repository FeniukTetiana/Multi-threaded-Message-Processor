package ua.shpp.feniuk.messaging;

import net.datafaker.Faker;
import ua.shpp.feniuk.pojo.PojoMessage;
import ua.shpp.feniuk.validation.EDDRValidator;

import java.time.LocalDateTime;

public class MessageGenerator {
    private static final Faker faker = new Faker();
    private static final EDDRValidator validator = new EDDRValidator();

    public PojoMessage generateRawMessage() {
        return new PojoMessage(
                faker.name().firstName(),
                validator.generateEDDR(),
                faker.number().numberBetween(1, 100),
                LocalDateTime.now()
        );
    }
}

