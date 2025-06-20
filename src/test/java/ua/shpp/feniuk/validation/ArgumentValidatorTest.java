package ua.shpp.feniuk.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentValidatorTest {
    @Test
    void validateArgs_withEmptyArgs_returnsFalse() {
        String[] args = {};
        assertFalse(ArgumentValidator.validateArgs(args));
    }

    @Test
    void validateArgs_withNonEmptyArgs_returnsTrue() {
        String[] args = {"10"};
        assertTrue(ArgumentValidator.validateArgs(args));
    }

    @Test
    void parseNumberOfMessages_withValidNumber_returnsParsedInt() {
        assertEquals(123, ArgumentValidator.parseNumberOfMessages("123"));
    }

    @Test
    void parseNumberOfMessages_withInvalidNumber_returnsMinusOne() {
        assertEquals(-1, ArgumentValidator.parseNumberOfMessages("abc"));
    }
}