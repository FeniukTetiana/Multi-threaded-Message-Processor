package ua.shpp.feniuk.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shpp.feniuk.pojo.PojoMessage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWriterService.class);
    private static final String VALID_MESSAGES_FILE = "valid_messages.csv";
    private static final String INVALID_MESSAGES_FILE = "invalid_messages.csv";

    protected static CSVPrinter validPrinter;
    protected static CSVPrinter invalidPrinter;

    static {
        try {
            BufferedWriter validWriter = new BufferedWriter(new FileWriter(VALID_MESSAGES_FILE, false));
            CSVFormat validFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("Name", "Count")
                    .build();
            validPrinter = new CSVPrinter(validWriter, validFormat);

            BufferedWriter invalidWriter = new BufferedWriter(new FileWriter(INVALID_MESSAGES_FILE, false));
            CSVFormat invalidFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("Name", "Count", "Errors")
                    .build();
            invalidPrinter = new CSVPrinter(invalidWriter, invalidFormat);
        } catch (IOException e) {
            LOGGER.error("Failed to initialize printers: ", e);
        }
    }

    public void saveValidMessageToFile(PojoMessage pojoMessage) throws IOException {
        validPrinter.printRecord(pojoMessage.getName(), pojoMessage.getCount());
    }

    public void saveInvalidMessageToFile(PojoMessage pojoMessage) throws IOException {
        String errorsJson = "{errors:[" + pojoMessage.getErrors() + "]}";
        invalidPrinter.printRecord(pojoMessage.getName(), pojoMessage.getCount(), errorsJson);
    }

    public static void closePrinters() {
        try {
            if (validPrinter != null) {
                validPrinter.flush();
                validPrinter.close();
                validPrinter = null;
            }
            if (invalidPrinter != null) {
                invalidPrinter.flush();
                invalidPrinter.close();
                invalidPrinter = null;
            }
            LOGGER.info("FileWriterService: Printers closed successfully.");
        } catch (IOException e) {
            LOGGER.error("Error while closing printers: ", e);
        }
    }
}





