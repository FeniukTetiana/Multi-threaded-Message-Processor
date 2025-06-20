package ua.shpp.feniuk.messaging;

import org.junit.jupiter.api.Test;
import ua.shpp.feniuk.pojo.PojoMessage;

import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

class MessageGeneratorTest {
    private final MessageGenerator generator = new MessageGenerator();

    @Test
    void testGenerateRawMessage_NotNullFields() {
        PojoMessage message = generator.generateRawMessage();

        assertNotNull(message.getName(), "Name should not be null");
        assertNotNull(message.getEddr(), "EDDR should not be null");
        assertTrue(message.getCount() >= 1 && message.getCount() <= 100,
                "Number should be between 1 and 100");
        assertNotNull(message.getCreatedAt(), "CreatedAt should not be null.");
    }

    @Test
    void testGenerateRawMessage_TimestampIsNow() {
        LocalDateTime before = LocalDateTime.now();
        PojoMessage message = generator.generateRawMessage();
        LocalDateTime after = LocalDateTime.now();

        assertTrue(!message.getCreatedAt().isBefore(before) && !message.getCreatedAt().isAfter(after),
                "Timestamp should be within the test execution time range");
    }

    @Test
    void testGenerateRawMessage_Uniqueness() {
        PojoMessage m1 = generator.generateRawMessage();
        PojoMessage m2 = generator.generateRawMessage();

        assertNotEquals(m1.getName(), m2.getName(), "Names should generally be different");
        assertNotEquals(m1.getEddr(), m2.getEddr(), "EDDRs should be different");
    }
}