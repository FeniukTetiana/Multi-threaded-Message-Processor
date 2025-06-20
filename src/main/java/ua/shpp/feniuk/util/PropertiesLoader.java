package ua.shpp.feniuk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shpp.feniuk.exception.PropertiesLoadingException;

import java.io.*;
import java.util.Properties;

public class PropertiesLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesLoader.class);
    private final Properties properties = new Properties();

    public PropertiesLoader(String fileName) throws PropertiesLoadingException, IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                properties.load(input);
                LOGGER.debug("Loaded properties from {}", fileName);
            } else {
                String errorMessage = "Properties file not found: " + fileName;
                LOGGER.error(errorMessage);
                throw new PropertiesLoadingException(errorMessage);
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}