package ua.shpp.feniuk.util;

import org.junit.jupiter.api.Test;
import ua.shpp.feniuk.exception.PropertiesLoadingException;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesLoaderTest {
    @Test
    void testLoadExistingPropertiesFile() throws Exception {
        PropertiesLoader loader = new PropertiesLoader("test.properties");
        String value = loader.getProperty("myKey");
        assertEquals("myValue", value);
    }

    @Test
    void testFileNotFound_throwsPropertiesLoadingException() {
        PropertiesLoadingException thrown = assertThrows(
                PropertiesLoadingException.class,
                () -> new PropertiesLoader("nonexistent.properties")
        );
        assertTrue(thrown.getMessage().contains("Properties file not found"));
    }
}