package ua.shpp.feniuk.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EDDRValidatorTest {
    private EDDRValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EDDRValidator();
    }

    @Test
    void isValid_nullOrBlank_returnsFalse() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("   "));
    }

    @Test
    void isValid_wrongLengthOrNonDigits_returnsFalse() {
        assertFalse(validator.isValid("123"));
        assertFalse(validator.isValid("12345678901234"));
        assertFalse(validator.isValid("123456789012a3"));
        assertFalse(validator.isValid("123456789012"));
    }

    @Test
    void isValid_validEDDR_returnsTrue() {
        String validEddr = validator.generateValidEDDR();
        assertTrue(validator.isValid(validEddr), "Generated valid EDDR should pass validation");
    }

    @Test
    void isValid_invalidEDDR_returnsFalse() {
        String invalidEddr = validator.generateInvalidEDDR();
        assertFalse(validator.isValid(invalidEddr), "Generated invalid EDDR should fail validation");
    }

    @Test
    void generateEDDR_returnsValidOrInvalidFormat() {
        for (int i = 0; i < 100; i++) {
            String eddr = validator.generateEDDR();
            String digits = eddr.replace("-", "");
            assertEquals(13, digits.length());
            assertTrue(digits.matches("\\d{13}"));
        }
    }
}