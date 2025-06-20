package ua.shpp.feniuk.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgumentValidator {
    ////
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentValidator.class);

    private ArgumentValidator() {
        // private constructor so that no instances of the class are created
    }

    public static boolean validateArgs(String[] args) {
        if (args.length == 0) {
            LOGGER.error("Please provide the number of messages as a command line argument");
            return false;
        }
        return true;
    }

    public static int parseNumberOfMessages(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid number format for messages: {}", arg);
            return -1;
        }
    }
}

