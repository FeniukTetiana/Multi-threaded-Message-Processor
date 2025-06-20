package ua.shpp.feniuk;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.shpp.feniuk.exception.PropertiesLoadingException;
import ua.shpp.feniuk.messaging.MessageProcessingService;
import ua.shpp.feniuk.util.PropertiesLoader;
import ua.shpp.feniuk.validation.ArgumentValidator;

import java.util.concurrent.TimeUnit;

public class MainMultiThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainMultiThread.class);
    private static final String PROPERTIES_FILE_NAME = "application.properties";

    public static void main(String[] args) {
        LOGGER.info("Start of multi-threaded program.");
        Stopwatch stopwatch = Stopwatch.createStarted();

        if (!ArgumentValidator.validateArgs(args)) return;
        int numberOfMessages = ArgumentValidator.parseNumberOfMessages(args[0]);
        if (numberOfMessages < 0) return;

        try {
            PropertiesLoader properties = new PropertiesLoader(PROPERTIES_FILE_NAME);
            int stopTimeSeconds = Integer.parseInt(properties.getProperty("stop.time.seconds"));

            new MessageProcessingService().processMessages(properties, numberOfMessages, stopTimeSeconds);
        } catch (PropertiesLoadingException e) {
            LOGGER.error("Failed to load properties file: ", e);
            return;
        } catch (Exception e) {
            LOGGER.error("An error occurred: ", e);
        }

        stopwatch.stop();
        double totalExecutionTime = stopwatch.elapsed(TimeUnit.SECONDS);
        double messagesPerSecond = numberOfMessages / totalExecutionTime;

        LOGGER.info("Finished multi-threaded program.");
        LOGGER.info("Total processing speed: {} MPS", Math.round(messagesPerSecond));
        LOGGER.info("Total execution time: {} seconds", totalExecutionTime);
    }
}


