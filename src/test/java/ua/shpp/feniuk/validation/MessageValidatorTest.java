package ua.shpp.feniuk.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.shpp.feniuk.pojo.PojoMessage;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageValidatorTest {
    private MessageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MessageValidator();
    }

    @Test
    void isNoConfirmedErrors_validMessage_returnsTrue() {
        PojoMessage msg = new PojoMessage();
        msg.setName("Natalia");
        msg.setCount(15);
        msg.setEddr("1990010100012");
        msg.setCreatedAt(LocalDateTime.now());

        boolean result = validator.isNoConfirmedErrors(msg);

        System.out.println("errors = " + msg.getErrors());

        assertTrue(result);
    }

    @Test
    void isNoConfirmedErrors_invalidMessage_returnsFalseAndSetsErrors() {
        PojoMessage message = new PojoMessage();

        boolean result = validator.isNoConfirmedErrors(message);

        assertFalse(result);
        assertNotNull(message.getErrors());
        assertFalse(message.getErrors().isBlank());
        System.out.println("Validation errors: " + message.getErrors());
    }
}